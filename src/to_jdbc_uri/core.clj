;;;; Copyright © 2015 Carousel Apps, Ltd

(ns to-jdbc-uri.core
  (:require [clojure.string :as s]))

(defn- format-credentials [uri]
  (when (.getUserInfo uri)
    (let [[username password] (s/split (.getUserInfo uri) #":")]
      (when-let [user-and-pass
                 (->> [(when username (str "user=" username))
                       (when password (str "password=" password))]
                      (remove nil?)
                      (s/join "&")
                      (not-empty))]
        (str "?" user-and-pass)))))

(defn- port [port]
  (when-not (neg? port)
    port))

(defn- host-and-port [uri]
  (s/join ":" (remove nil? [(.getHost uri) (port (.getPort uri))])))

(defn- format-jdbc-uri [uri db]
  (str "jdbc:" db "://"
       (host-and-port uri)
       (.getPath uri)
       (format-credentials uri)))

(defn to-jdbc-uri
  "Convert a non-JDBC URI to a JDBC one."
  [uri]
  (when (empty? uri)
    (throw (Exception. "URI connection string cannot be empty!")))
  (if (.startsWith uri "jdbc")
    uri
    (let [parsed-uri (java.net.URI. uri)]
      (case (.getScheme parsed-uri)
        ;; OPENSHIFT_POSTGRESQL_DB_URL starts with postgresql:// instead of postgres://
        (or "postgres" "postgresql") (format-jdbc-uri parsed-uri "postgresql")
        "mysql" (format-jdbc-uri parsed-uri "mysql")
        (throw (Exception. (str "Unsupported URI: " uri " please, submit an issue request and we'll try to add it. Pull requests also welcome")))))))
