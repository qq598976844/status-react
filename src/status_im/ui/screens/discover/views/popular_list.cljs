(ns status-im.ui.screens.discover.views.popular-list
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require
    [re-frame.core :refer [subscribe dispatch]]
    [status-im.components.react :refer [view
                                        list-view
                                        list-item
                                        touchable-highlight
                                        text]]
    [status-im.ui.screens.discover.styles :as st]
    [status-im.utils.listview :refer [to-datasource]]
    [status-im.ui.screens.discover.views.discover-list-item :refer [discover-list-item]]
    [status-im.utils.platform :refer [platform-specific]]))

(defview popular-hashtag-status-preview [{:keys [tag current-account]}]
  (letsubs [discoveries [:get-popular-discoveries 1 [tag]]]
    [view (merge st/popular-list-container
                 #_(get-in platform-specific [:component-styles :discover :popular]))
     [view st/row
      [view (get-in platform-specific [:component-styles :discover :tag])
       [touchable-highlight {:on-press #(do (dispatch [:set :discover-search-tags [tag]])
                                            (dispatch [:navigate-to :discover-search-results]))}
        [view
         [text {:style st/tag-name
                :font  :medium}
          (str " #" (name tag))]]]]
      [view st/tag-count-container
       [text {:style st/tag-count
              :font  :default}
        (:total discoveries)]]]
       [discover-list-item {:message         (first (:discoveries discoveries))
                            :show-separator? false
                            :current-account current-account}]]))
