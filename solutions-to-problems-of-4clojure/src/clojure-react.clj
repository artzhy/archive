(declare component div ul li RegionsLayer LAYERS.REGION Layer)
(declare default-value http-request:get http-request:post form input input:file)
(declare RootFolder thunks:upload-files-to-remote thunks:load-folder-info)
(declare alert console.log confirm console.error serialize def-component component-name)

(defn thunks:upload-files-to-remote [parent-id files]
  (fn [dispatch]
    (http-request:post
      "/api/files/upload"
      (let [files-form-data (map-indexed
                              (fn [index file]
                                {(str "file-" index) file})
                              files)
            form-data (into {:parent_id parent-id
                             "count"    (count files)
                             } files-form-data)]
        {:body        (serialize form-data)
         :credentials "include"}

        :then
        (fn [response]
          (if (response :ok)
            (dispatch (thunks:load-folder-info parent-id))
            (do (alert "请求出错")
                (console.error response))
            ))))))

(def RegionsLayer
  :prop-types
  {
   :info   :map!
   :config :map!
   :sids   :set!
   }

  :render
  (fn [[sids config info]]
    (let [rects
          (map-indexed
            (fn [id item]
              (div {:key id :info item :selected (sids id)}
                   "Children of div"
                   "Another children of the div"))
            info)]
      (Layer {:config config :name LAYERS.REGION}
             rects))))

(def-component component-name
  :props {:info                    :map!
          :config                  :map!
          :sids                    :set!
          :prop-with-default-value {:type :number :defualt default-value}
          }

  :did-update #(http-request:get "www.baidu.com")

  :should-update? (constantly true)

  :on-context-menu nil
  :change (fn [{:keys [state context]} event]
            (if-let [files (event :target :file)]
              (let [parent-id (state :cinfo :folder-id)
                    dispatch (context :store :dispatch)]
                (dispatch (thunks:upload-files-to-remote parent-id files)))))

  :render
  (fn [{:keys [on-context-menu change]}]
    (div
      {:class "structure" :on-context-menu on-context-menu}
      (RootFolder {})
      (form
        {:ref "form"}
        (input:file
          {:ref       "upload-files-input"
           :class     "file-input"
           :multiple  true
           :on-change change})))
    ))