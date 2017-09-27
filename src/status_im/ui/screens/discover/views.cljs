(ns status-im.ui.screens.discover.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require
    [re-frame.core :refer [dispatch subscribe]]
    [clojure.string :as str]
    [status-im.components.react :refer [view
                                        scroll-view
                                        text
                                        text-input
                                        touchable-highlight]]
    [status-im.components.icons.vector-icons :as vi]
    [status-im.components.toolbar-new.view :refer [toolbar-with-search]]
    [status-im.components.toolbar-new.actions :as act]
    [status-im.components.drawer.view :as drawer]
    [status-im.components.carousel.carousel :refer [carousel]]
    [status-im.ui.screens.discover.views.popular-list :refer [popular-hashtag-status-preview]]
    [status-im.ui.screens.discover.views.discover-list-item :refer [discover-list-item]]
    [status-im.ui.screens.discover.views.search-results :refer [tags-menu]]
    [status-im.utils.platform :refer [platform-specific]]
    [status-im.i18n :refer [label]]
    [status-im.ui.screens.discover.styles :as st]
    [status-im.ui.screens.contacts.styles :as contacts-st]
    [status-im.components.toolbar-new.view :as toolbar]
    [status-im.ui.screens.discover.styles :as st]))

(defn get-hashtags [status]
  (let [hashtags (map #(str/lower-case (str/replace % #"#" "")) (re-seq #"[^ !?,;:.]+" status))]
    (or hashtags [])))

(defn toolbar-view [show-search? search-text]
  [toolbar-with-search
   {:show-search?       show-search?
    :search-text        search-text
    :search-key         :discover
    :title              (label :t/discover)
    :search-placeholder (label :t/search-tags)
    :nav-action         (act/hamburger drawer/open-drawer!)
    :on-search-submit   (fn [text]
                          (when-not (str/blank? text)
                            (let [hashtags (get-hashtags text)]
                              (dispatch [:set :discover-search-tags hashtags])
                              (dispatch [:navigate-to :discover-search-results]))))}])

(defn title [label-kw spacing? action-kw action-fn]
  [view (merge st/title
               (when spacing? {:margin-top 16}))
   [text {:style      (get-in platform-specific [:component-styles :discover :subtitle])
          :uppercase? (get-in platform-specific [:discover :uppercase-subtitles?])
          :font       :medium}
    (label label-kw)]
   [touchable-highlight {:on-press action-fn}
    [view {} [text {:style st/title-action-text} (label action-kw)]]]])

(defview discover-all-hashtags [{:keys [contacts current-account]}]
  (letsubs [popular-tags          [:get-popular-tags 10]
            {:keys [discoveries]} [:get-popular-discoveries 10]]  ;uses the tags passed via :discover-search-tags state
     [view st/discover-container
      [toolbar/toolbar2 {}
       toolbar/default-nav-back
       [view {} [text {} "All hashtags"]]]
      [tags-menu (map :name popular-tags)]
      [scroll-view st/list-container
       [view st/recent-container
        [view st/recent-list
         (let [discoveries (map-indexed vector discoveries)]
           (for [[i {:keys [message-id] :as message}] discoveries]
             ^{:key (str "message-hashtag-" message-id)}
             [discover-list-item {:message         message
                                  :show-separator? (not= (inc i) (count discoveries))
                                  :current-account current-account}]))]]]]))

(defview popular-hashtags-preview [{:keys [contacts current-account]}]
  (letsubs [popular-tags [:get-popular-tags 10]]
    [view st/popular-container
     [title :t/popular-tags false :t/all #(do (dispatch [:set :discover-search-tags (map :name popular-tags)])
                                              (dispatch [:navigate-to :discover-all-hashtags]))]
     (if (pos? (count popular-tags))
       [carousel {:pageStyle st/carousel-page-style
                  :gap       8
                  :sneak     16
                  :count     (count popular-tags)}
        (for [{:keys [name]} popular-tags]
          [popular-hashtag-status-preview {:tag             name
                                           :contacts        contacts
                                           :current-account current-account}])]
       [text (label :t/none)])]))


(defview discover-all-recent []
  (letsubs [discoveries     [:get-recent-discoveries]
            tabs-hidden?    [:tabs-hidden?]
            current-account [:get-current-account]]
    (when (seq discoveries)
      [view st/discover-container
       [toolbar/toolbar2 {}
        toolbar/default-nav-back
        [view {} [text {} "All recent"]]]
       [scroll-view (st/list-container tabs-hidden?)
         [view st/recent-container
          [view st/recent-list
           (let [discoveries (map-indexed vector discoveries)]
             (for [[i {:keys [message-id] :as message}] discoveries]
               ^{:key (str "message-recent-" message-id)}
               [discover-list-item {:message         message
                                    :show-separator? (not= (inc i) (count discoveries))
                                    :current-account current-account}]))]]]])))

(defn empty-discoveries []
  [view contacts-st/empty-contact-groups
   ;; todo change icon
   [vi/icon :icons/group-big {:style contacts-st/empty-contacts-icon}]
   [text {:style contacts-st/empty-contacts-text}
    (label :t/no-statuses-discovered)]])

(defn recent-statuses-preview [current-account discoveries]
  [view st/recent-statuses-preview-container
   [title :t/recent false :t/all #(dispatch [:navigate-to :discover-all-recent])]
   (if (pos? (count discoveries))
     [carousel {:pageStyle st/carousel-page-style
                :gap       8
                :sneak     16
                :count     (count discoveries)}
      (for [discovery discoveries]
        [view st/recent-statuses-preview-content
         [discover-list-item {:message         discovery
                              :show-separator? false
                              :current-account current-account}]])]
     [text (label :t/none)])])

(defview discover [current-view?]
  (letsubs [show-search     [:get-in [:toolbar-search :show]]
            search-text     [:get-in [:toolbar-search :text]]
            contacts        [:get-contacts]
            current-account [:get-current-account]
            discoveries     [:get-recent-discoveries]]
    [view st/discover-container
     [toolbar-view (and current-view?
                        (= show-search :discover)) search-text]
     (if discoveries
       [scroll-view st/list-container
        [recent-statuses-preview current-account discoveries]
        [popular-hashtags-preview {:contacts        contacts
                                   :current-account current-account}]]
       [empty-discoveries])]))
