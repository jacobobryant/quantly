(ns quantly.service
  (:require [io.pedestal.http :as http]
            [io.pedestal.http.route :as route]
            [io.pedestal.http.body-params :as body-params]
            [io.pedestal.interceptor :as interceptor]
            [ring.util.response :as ring-resp]
            [datomic.api :as d]
            [clojure.pprint :refer [pprint]]
            [io.pedestal.interceptor :as interceptor]
            [io.pedestal.interceptor.helpers :as h]))

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
  "Provide a Datomic conn and db in all incoming requests"
  (interceptor/interceptor
   {:name ::insert-datomic
    :enter (fn [context]
             (-> context
                 (assoc-in [:request :conn] conn)
                 (assoc-in [:request :db] (d/db conn))))}))

(h/defhandler hello
  [request]
  (let [message (-> (d/q '[:find ?msg
                           :where [?id :conf :main]
                                  [?id :message ?msg]]
                         (:db request))
                    first
                    first)]
    {:status 200
     :body message}))

(h/defbefore set-hello
  [ctx]
  (if-let [message (get-in ctx [:request :edn-params :message])]
    (let [conn (get-in ctx [:request :conn])
          id (d/tempid :db.part/user)
          datoms [[:db/add id :message message]
                  [:db/add id :conf :main]]
          tx-result @(d/transact conn datoms)]
      (assoc-in ctx [:request :db] (:db-after tx-result)))
    ctx))

(defn routes
  [conn]
  (route/expand-routes
    [[["/" {:get hello
            :patch [:set-hello ^:interceptors [set-hello] hello]}
       ^:interceptors [(body-params/body-params) (insert-datomic conn) http/html-body]]]]))

(defn service
  [uri]
  (let [conn (d/connect uri)]
    {:env :prod
     ::http/routes (routes conn)
     ::http/resource-path "/public"
     ::http/type :jetty
     ::http/port 8080
     ::http/container-options {:h2c? true
                               :h2? false
                               :ssl? false}}))
