(ns demo.core
  (:gen-class)
  (:require [org.httpkit.server :as http]
            [compojure.core :refer :all]
            [selmer.parser :refer [render render-file]]
            [compojure.route :as route]))


(defonce channels (atom {}))


(defn add-channel [chan username]
  (swap! channels assoc chan username))


(defn del-channel [chan]
  (swap! channels dissoc chan))


(defn eval-message [message]
  (try
    (let [result (with-out-str (prn (eval (read-string message))))]
      (str message " => " result))
    (catch Exception _ message)))


(defn format-message [username message]
  (render
    "<div><b>{{ username }}</b> <pre>{{ message }}</pre></div>"
    {:username username :message (eval-message message)}))


(defn broadcast [username message]
  (let [formatted-message (format-message username message)]
    (doseq [[chan] @channels]
      (http/send! chan formatted-message))))


(defn simple-handler [request]
  {:status 200
   :body   "Hello World!"})


(defn page-handler [request]
  {:status 200
   :body   (render-file
             "index.html"
             {:title (-> request :params :username)})})


(defn websocket-handler [request]
  (let [username (-> request :params :username)]
    (http/with-channel request chan
      (add-channel chan username)
      (http/on-close chan (fn [_] (del-channel chan)))
      (http/on-receive chan (fn [msg] (broadcast username msg))))))


(def handler
  (routes
    (GET "/" req (simple-handler req))
    (GET "/chat/:username" req (page-handler req))
    (GET "/websocket/:username" req (websocket-handler req))
    (route/resources "/assets")))


(defn -main []
  (http/run-server #'handler {:port 3000})
  (println "Web server started at localhost:3000"))
