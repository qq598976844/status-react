(ns status-im.ui.screens.discover.views.search-results
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [re-frame.core :refer [subscribe dispatch]]
            [status-im.utils.listview :refer [to-datasource]]
            [status-im.components.status-bar :refer [status-bar]]
            [status-im.components.react :refer [view
                                                text
                                                list-view
                                                list-item
                                                scroll-view
                                                touchable-highlight]]
            [status-im.components.icons.vector-icons :as vi]
            [status-im.components.toolbar.view :refer [toolbar]]
            [status-im.components.toolbar.actions :as act]
            [status-im.ui.screens.discover.views.components :refer [discover-list-item]]
            [status-im.utils.platform :refer [platform-specific]]
            [status-im.i18n :refer [label]]
            [status-im.ui.screens.discover.styles :as st]
            [status-im.ui.screens.contacts.styles :as contacts-st]
            [status-im.components.toolbar-new.view :as toolbar]))

(defn render-separator [_ row-id _]
  (list-item [view {:style st/row-separator
                    :key   row-id}]))


(defview discover-search-results []
  (letsubs [discoveries     [:get-popular-discoveries 250]
            tags            [:get :discover-search-tags]
            current-account [:get-current-account]]
    (let [discoveries (:discoveries discoveries)
          datasource  (to-datasource discoveries)]
      [view st/discover-tag-container
       [status-bar]
       [toolbar/toolbar2 {}
        toolbar/default-nav-back
        [view {:flex-direction  :row
               :justify-content :flex-start}
         [text {} (str "#" (first tags))]]]
       (if (empty? discoveries)
         [view st/empty-view
          [vi/icon :icons/group-big {:style contacts-st/empty-contacts-icon}]
          [text {:style contacts-st/empty-contacts-text}
           (label :t/no-statuses-found)]]
         [list-view {:dataSource      datasource
                     :renderRow       (fn [row _ _]
                                        (list-item [discover-list-item
                                                    {:message         row
                                                     :current-account current-account}]))
                     :renderSeparator render-separator
                     :style           st/recent-list}])])))
