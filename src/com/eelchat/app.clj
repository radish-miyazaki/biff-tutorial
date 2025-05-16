(ns com.eelchat.app
  (:require
   [com.biffweb :as biff]
   [xtdb.api :as xt]
   [com.eelchat.ui :as ui]
   [com.eelchat.middleware :as mid]))

(defn app [{:keys [session biff/db] :as ctx}]
  (let [{:user/keys [email]} (xt/entity db (:uid session))]
    (ui/page
     {}
     [:div "Signed in as " email ". "
      (biff/form
       {:action "/auth/signout"
        :class "inline"}
       [:button.text-blue-500.hover:text-blue-800 {:type "submit"} "Sign out"])
      "."]
     [:div.h-6]
     [:div "Thanks for joining the waitlist. "
      "We'll let you know when eelchat is ready to use."])))

(def module
  {:routes ["/app" {:middleware [mid/wrap-signed-in]}
            ["" {:get app}]]})
