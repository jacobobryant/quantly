(ns quantly.server
  (:gen-class)
  (:require [io.pedestal.http :as http]
            [quantly.service :as service]
            [datomic.api :as d]))

(def uri "datomic:dev://localhost:4334/quantly")

(defn run-dev
  [& args]
  (println "\nCreating your [DEV] server...")
  (service/ensure-schema uri)
  (let [conn (d/connect uri)]
    (-> (service/service uri)
        (merge {:env :dev
                ::http/join? false
                ::http/routes #(service/routes conn)
                ::http/allowed-origins {:creds true :allowed-origins (constantly true)}
                ::http/secure-headers {:content-security-policy-settings {:object-src "'none'"}}})
        http/default-interceptors
        http/dev-interceptors
        http/create-server
        http/start)))

(defn -main
  [& args]
  (println "\nCreating your server...")
  (service/ensure-schema uri)
  (-> uri
      service/service
      http/create-server
      http/start))
