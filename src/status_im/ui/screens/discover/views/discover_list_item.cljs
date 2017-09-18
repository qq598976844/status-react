(ns status-im.ui.screens.discover.views.discover-list-item
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [re-frame.core :refer [subscribe dispatch]]
            [clojure.string :as str]
            [status-im.components.react :refer [view text image touchable-highlight]]
            [status-im.ui.screens.discover.styles :as st]
            [status-im.components.status-view.view :refer [status-view]]
            [status-im.utils.gfycat.core :refer [generate-gfy]]
            [status-im.utils.identicon :refer [identicon]]
            [status-im.components.chat-icon.screen :as ci]
            [status-im.utils.platform :refer [platform-specific]]))

(defn display-name [me? account-name contact-name name whisper-id]
  (cond
    me? account-name ;status by current user
    (not (str/blank? contact-name)) contact-name    ; what's the
    (not (str/blank? name)) name                    ;difference
    :else (generate-gfy whisper-id)))

(defn display-image [me? account-photo-path contact-photo-path photo-path whisper-id]
  (cond
    me? account-photo-path
    (not (str/blank? contact-photo-path)) contact-photo-path
    (not (str/blank? photo-path)) photo-path
    :else (identicon whisper-id)))

(defview discover-list-item [{:keys [message show-separator? current-account]}]
  (letsubs [{contact-name       :name
             contact-photo-path :photo-path} [:get-in [:contacts/contacts (:whisper-id message)]]]
    (let [{:keys [name photo-path whisper-id message-id status]} message
          {account-photo-path :photo-path
           account-address    :public-key
           account-name       :name} current-account
          me? (= account-address whisper-id)
          item-style (get-in platform-specific [:component-styles :discover :item])]
      [view
       [view st/popular-list-item
        [status-view {:id     message-id
                      :style  (:status-text item-style)
                      :status status}]
        [view st/popular-list-item-name-container
         [view (merge st/popular-list-item-avatar-container
                      (:icon item-style))
          [ci/chat-icon
           (display-image me? account-photo-path contact-photo-path photo-path whisper-id)
           {:size 20}]]
         [text {:style           st/popular-list-item-name
                :font            :medium
                :number-of-lines 1}
          (display-name me? account-name contact-name name whisper-id)]]
       (when show-separator?
         [view st/separator])]])))
