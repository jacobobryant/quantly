(ns quantly.service
  (:require [io.pedestal.http :as http]
            [io.pedestal.http.route :as route]
            [io.pedestal.http.body-params :as body-params]
            [io.pedestal.http.params :refer [keyword-params]]
            [datomic.api :as d]
            [clojure.pprint :refer [pprint]]
            [io.pedestal.interceptor.helpers :as h]
            [hiccup.core :refer [html]]
            [taoensso.sente :as sente]
            [taoensso.sente.server-adapters.immutant :refer [get-sch-adapter]]
            ))

(let [{:keys [ch-recv send-fn connected-uids
              ajax-post-fn ajax-get-or-ws-handshake-fn]}
      (sente/make-channel-socket! (get-sch-adapter) {})]

  (def ring-ajax-post                ajax-post-fn)
  (def ring-ajax-get-or-ws-handshake ajax-get-or-ws-handshake-fn)
  (def ch-chsk                       ch-recv) ; ChannelSocket's receive channel
  (def chsk-send!                    send-fn) ; ChannelSocket's send API fn
  (def connected-uids                connected-uids) ; Watchable, read-only atom
  )

(def schema [{:db/ident              :message
              :db/valueType          :db.type/string
              :db/cardinality        :db.cardinality/one}
             {:db/ident              :conf
              :db/valueType          :db.type/keyword
              :db/cardinality        :db.cardinality/one
              :db/unique             :db.unique/identity}])

(defn ensure-schema
  [uri]
  (d/create-database uri)
  @(d/transact (d/connect uri) schema))

(defn insert-datomic
  [conn]
  (h/before
    ::insert-datomic
    (fn [context]
      (-> context
          (assoc-in [:request :conn] conn)
          (assoc-in [:request :db] (d/db conn))))))

(def hello
  (h/handler
    ::hello
    (fn [request]
      (let [message (-> (d/q '[:find ?msg
                               :where [?id :conf :main]
                               [?id :message ?msg]]
                             (:db request))
                        first
                        first)]
        {:status 200
         :body message}))))

(def set-hello
  (h/before
    ::set-hello
    (fn [ctx]
      (if-let [message (get-in ctx [:request :edn-params :message])]
        (let [conn (get-in ctx [:request :conn])
              id (d/tempid :db.part/user)
              datoms [[:db/add id :message message]
                      [:db/add id :conf :main]]
              tx-result @(d/transact conn datoms)]
          (assoc-in ctx [:request :db] (:db-after tx-result)))
        ctx))))

(def main
  (h/handler
    ::main
    (fn [request]
      {:status 200
       :body (html [:html
                    {:lang "en"}
                    [:head [:meta {:charset "utf-8"}]]
                    [:body
                     [:div#app "hello"]
                     [:script {:src "cljs-out/quantly-main.js"}]
                     [:script "quantly.core.init();"]]])})))


(def sente-get
  (h/handler
    ::sente-get
    (fn [req]
      (ring-ajax-get-or-ws-handshake req))))

(def sente-post
  (h/handler
    ::sente-post
    (fn [req]
      (ring-ajax-post req))))

(defn routes
  [conn]
  (route/expand-routes
    [[["/" {:get hello
            :patch [:set-hello ^:interceptors [set-hello] hello]}
       ["/main" {:get main}]
       ["/chsk" {:get sente-get
                 :post sente-post}]
       ^:interceptors [(body-params/body-params) keyword-params (insert-datomic conn) http/html-body]
       ]]]))

(defn service
  [uri]
  (let [conn (d/connect uri)]
    {:env :prod
     ::http/routes (routes conn)
     ::http/resource-path "/public"
     ::http/type :immutant
     ::http/port 8080
     ::http/container-options {:h2c? true
                               :h2? false
                               :ssl? false}
     ::http/secure-headers {:content-security-policy-settings
                            {:object-src "none"}}
     }))
