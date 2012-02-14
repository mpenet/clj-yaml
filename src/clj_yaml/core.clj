(ns clj-yaml.core
  (:import (org.yaml.snakeyaml Yaml)))

(def ^{:private true} yaml (Yaml.))

(defprotocol YAMLCodec
  (encode [data])
  (decode [data]))

(extend-protocol YAMLCodec

  clojure.lang.IPersistentMap
  (encode [data]
    (into {}
          (for [[k v] data]
            [(encode k) (encode v)])))

  clojure.lang.IPersistentCollection
  (encode [data]
    (map encode data))

  java.lang.Object
  (encode [data] data)

  clojure.lang.Keyword
  (encode [data]
    (name data))

  java.util.LinkedHashMap
  (decode [data]
    (into {}
          (for [[k v] data]
            [(keyword k) (decode v)])))

  java.util.ArrayList
  (decode [data]
    (map decode data))

  java.lang.Object
  (decode [data] data))

(defn generate-string [data]
  (.dump yaml (encode data)))

(defn parse-string [string]
  (decode (.load yaml string)))
