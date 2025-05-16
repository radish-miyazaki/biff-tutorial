(ns com.eelchat.ui
  (:require [cheshire.core :as cheshire]
            [clojure.java.io :as io]
            [com.eelchat.settings :as settings]
            [com.biffweb :as biff]
            [ring.middleware.anti-forgery :as csrf]
            [ring.util.response :as ring-response]
            [rum.core :as rum]))

(defn static-path [path]
  (if-some [last-modified (some-> (io/resource (str "public" path))
                                  ring-response/resource-data
                                  :last-modified
                                  (.getTime))]
    (str path "?t=" last-modified)
    path))

(defn base [{:keys [::recaptcha] :as ctx} & body]
  (apply
   biff/base-html
   (-> ctx
       (merge #:base{:title settings/app-name
                     :lang "en-US"
                     :icon "/img/glider.png"
                     :description "The world's finest discussion platform"
                     :image "https://clojure.org/images/clojure-logo-120b.png"})
       (update :base/head (fn [head]
                            (concat [[:link {:rel "stylesheet" :href (static-path "/css/main.css")}]
                                     [:script {:src (static-path "/js/main.js")}]
                                     [:script {:src "https://unpkg.com/htmx.org@2.0.4"}]
                                     [:script {:src "https://unpkg.com/htmx-ext-ws@2.0.1/ws.js"}]
                                     [:script {:src "https://unpkg.com/hyperscript.org@0.9.13"}]
                                     [:link {:href "/apple-touch-icon.png" :size "180x180" :rel "apple-touch-icon"}]
                                     [:link {:href "/favicon-32x32.png" :size "32x32" :rel "icon"}]
                                     [:link {:href "/favicon-16x16.png" :size "16x16" :rel "icon"}]
                                     [:link {:href "/site.webmanifest" :rel "manifest"}]
                                     [:link {:color "#5bbad5" :href "/safari-pinned-tab.svg" :rel "mask-icon"}]
                                     [:meta {:content "#da532c" :name "msapplication-TileColor"}]
                                     [:meta {:content "#0d9488" :name "theme-color"}]
                                     (when recaptcha
                                       [:script {:src "https://www.google.com/recaptcha/api.js"
                                                 :async "async" :defer "defer"}])]
                                    head))))
   body))

(defn page [ctx & body]
  (base
   ctx
   [:.bg-orange-50.flex.flex-col.flex-grow
    [:.flex-grow]
    [:.p-3.mx-auto.max-w-screen-sm.w-full
     (when (bound? #'csrf/*anti-forgery-token*)
       {:hx-headers (cheshire/generate-string
                     {:x-csrf-token csrf/*anti-forgery-token*})})
     body]
    [:.flex-grow]
    [:.flex-grow]]))

(defn on-error [{:keys [status _] :as ctx}]
  {:status status
   :headers {"content-type" "text/html"}
   :body (rum/render-static-markup
          (page
           ctx
           [:h1.text-lg.font-bold
            (if (= status 404)
              "Page not found."
              "Something went wrong.")]))})
