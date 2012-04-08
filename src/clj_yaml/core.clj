(ns clj-yaml.core
  (:import [org.yaml.snakeyaml Yaml]
           [org.yaml.snakeyaml DumperOptions]
           [org.yaml.snakeyaml DumperOptions$FlowStyle]))

(def ^{:private true} yaml (Yaml.))

(def ^{:dynamic true} *keywordize* true)

(defn set-flow-style [flow-style]
  (let [flow-style (cond (re-find #"(?i)block" flow-style) DumperOptions$FlowStyle/BLOCK
                         (re-find #"(?i)flow" flow-style) DumperOptions$FlowStyle/FLOW
                         (re-find #"(?i)auto" flow-style) DumperOptions$FlowStyle/AUTO)]
    (def ^{:private true} clj-yaml.core/dumper-options (DumperOptions.))
    (. clj-yaml.core/dumper-options (setDefaultFlowStyle flow-style))
    (def ^{:private true} clj-yaml.core/yaml (Yaml. dumper-options))))

(defprotocol YAMLCodec
  (encode [data])
  (decode [data]))

(defn decode-key [k]
  (if *keywordize* (keyword k) k))

(extend-protocol YAMLCodec

  clojure.lang.IPersistentMap
  (encode [data]
    (into {}
          (for [[k v] data]
            [(encode k) (encode v)])))

  clojure.lang.IPersistentCollection
  (encode [data]
    (map encode data))

  clojure.lang.Keyword
  (encode [data]
    (name data))

  java.util.LinkedHashMap
  (decode [data]
    (into {}
          (for [[k v] data]
            [(decode-key k) (decode v)])))

  java.util.LinkedHashSet
  (decode [data]
    (into #{} data))

  java.util.ArrayList
  (decode [data]
    (map decode data))

  Object
  (encode [data] data)
  (decode [data] data)

  nil
  (encode [data] data)
  (decode [data] data))

(defn generate-string [data]
  (.dump yaml (encode data)))

(defn parse-string
  ([string keywordize]
     (binding [*keywordize* keywordize]
       (parse-string string)))
  ([string]
     (decode (.load yaml string))))
