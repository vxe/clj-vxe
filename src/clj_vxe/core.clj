(ns clj-vxe.core
  (:require 
   [me.raynes.conch.low-level]
   [yaml.core]
   [clojure.data.xml]
   [clj-http.client]
   [cemerick.pomegranate]
   [java-time])
  (:use
   [clojure.java.shell :only [sh]]))

(defn expand-home [s]
  (if (.startsWith s "~")
    (clojure.string/replace-first s "~" (System/getProperty "user.home"))
    s))

(defn mkdir-p [path]
                  (clojure.java.io/make-parents (expand-home path)))

(defn pwd []
  (System/getProperty "user.dir"))

(defn ls-ltrh
          ([]
           (println (:out (sh "ls" "-ltrh"))))
          ([value]
           (:out (sh "ls" "-ltrh"))))

(defn date-pp
       ([]
        (println (:out (sh "date"))))
       ([format]
        (println (:out (sh "date" format))))
       ([format value]
        (:out (sh "date" format))))

(defn do-cron [duration task]
     (future (loop []
             (doall
             (task)
              (Thread/sleep duration))
             (recur))))

(defn now []
        (java.util.Date.))

(defn n-hours-from-now [n]
  (java-time/plus (java-time/local-time) (java-time/hours n)))

(defn n-mins-from-now [n]
  (java-time/plus (java-time/local-time) (java-time/minutes n)))

(defn mean [coll]
  (let [sum (apply + coll)
        count (count coll)
        ]
    (if (pos? count)
      (/ sum count)
      0)))

(defn standard-deviation [coll]
        (let [avg (mean coll)
              squares (for [x coll]
                        (let [x-avg (- x avg)]
                          (* x-avg x-avg)
                          )
                        )
              total (count coll)]

          (-> (/ (apply + squares)
                 (- total 1)
                 )
              (Math/sqrt)
              )
          )
        )

(defn mode [coll]
  (let [freqs (frequencies coll)
        occurrences (group-by second freqs)
        modes (last (sort occurrences))
        modes (->> modes
                   second
                   (map first))]
    modes))

(defn parse-int [string]
  (Integer/parseInt string))

(defn keywordize-map [map]
(into {} 
  (for [[k v] map] 
    [(keyword k) v]))
)

(defn grep-seq [regex sequence]
        (for [line sequence]
          (if (re-matches regex (.toString line))
            line)))

(defn capitalize-words 
  "Capitalize every word in a string"
  [s]
  (->> (clojure.string/split (str s) #"\b") 
       (map clojure.string/capitalize)
       clojure.string/join))

(defn xml-print [xml & file]
        (if (not (empty? file))
          (do
            (with-open [out-file (java.io.OutputStreamWriter.
                                  (java.io.FileOutputStream. (expand-home (first file)))
                                  "UTF-8")]
              (clojure.data.xml/emit
               (clojure.data.xml/sexp-as-element xml)
               out-file)))
          (clojure.data.xml/sexp-as-element xml)))

;; (defn xml-print-str [xml]
;;                   (clojure.data.xml/emit-str (xml-print xml)))
(defn xml-print-str [xml & file]
        (if (not (empty? file))
          (do
            (with-open [out-file (java.io.OutputStreamWriter.
                                  (java.io.FileOutputStream. (expand-home (first file)))
                                  "UTF-8")]
              (-> xml
                  clojure.data.xml/sexp-as-element
                  (clojure.data.xml/emit
                   out-file))))
          (-> xml
              clojure.data.xml/sexp-as-element
              clojure.data.xml/emit-str)))

(defn xml-print-stylesheet [stylesheet xml & file]
        (if (not (empty? file))
          (do
            (with-open [out-file (java.io.OutputStreamWriter.
                                  (java.io.FileOutputStream. (expand-home (first file)))
                                  "UTF-8")]

              (clojure.data.xml/emit
               (clojure.data.xml/sexps-as-fragment
                stylesheet
                xml)
               out-file)))
          (clojure.data.xml/sexp-as-element xml)))

;; (defn add-processing-instructions [xml & stylesheet]
;;                   (if (not (empty? stylesheet))
;;                     (let [stylesheet (xsl/stylesheet {:version 3.0}
;;                                                      (xsl/template {:match "/"}
;;                                                                    (xsl/processing-instruction {:name "xsl-stylesheet"} (str "href=" (first stylesheet)))
;;                                                                    (xsl/copy-of {:select "node()"})))]
;;                       (if (.exists (io/file (expand-home xml)))
;;                         (-> stylesheet
;;                             xslt/compile-sexp
;;                             (xslt/transform (io/as-file (expand-home xml))))
;;                         (-> stylesheet
;;                             xslt/compile-sexp
;;                             (xslt/transform xml))))))

;; (defn add-processing-instructions-file [xml output & stylesheet]
;;                   (if (not (empty? stylesheet))
;;                     (let [stylesheet (xsl/stylesheet {:version 3.0}
;;                                                      (xsl/template {:match "/"}
;;                                                                    (xsl/processing-instruction {:name "xsl-stylesheet"} (str "href=" (first stylesheet)))
;;                                                                    (xsl/copy-of {:select "node()"})))]
;;                       (if (.exists (io/file (expand-home xml)))
;;                         (-> stylesheet
;;                             xslt/compile-sexp
;;                             (xslt/transform-to-file  (io/as-file (expand-home xml)) (io/file (expand-home output))))
;;                         (let [temp-file (java.io.File/createTempFile (uuid) ".xml")]
;;                           (spit temp-file xml)
;;                           (println (.getAbsolutePath temp-file))
;;                           (-> stylesheet
;;                               xslt/compile-sexp
;;                               (xslt/transform-to-file (.getAbsolutePath temp-file) (io/file output))))))))

(defn uuid [] (str (java.util.UUID/randomUUID)))

(defn curl
  ([url]
   (:body (clj-http.client/get url)))
  ([url headers]
   (:body (clj-http.client/get url headers))))

(defn copy-uri-to-file [uri file]
  (with-open [in (clojure.java.io/input-stream uri)
              out (clojure.java.io/output-stream file)]
    (clojure.java.io/copy in out)))

(defn get-clipboard []
  (.getSystemClipboard (java.awt.Toolkit/getDefaultToolkit)))

(defn slurp-clipboard []
  (try
    (.getTransferData (.getContents (get-clipboard) nil) (java.awt.datatransfer.DataFlavor/stringFlavor))
    (catch java.lang.NullPointerException e nil)))

(defn spit-clipboard [text]
  (.setContents (get-clipboard) (java.awt.datatransfer.StringSelection. text) nil))

(defn yaml-pp
  ([data] (println (yaml.core/generate-string data :dumper-options {:flow-style :block})))
  ([data path]   (spit (expand-home path) (yaml.core/generate-string data :dumper-options {:flow-style :block}))))

(defn assoc-append [m k v]
(if (contains? m k)
  (assoc m k (into {} [(k m)
                       v]))
  (assoc m k v)))

(defn merge-append [m1 m2]
          (first (remove nil? (for [[k2 v2] m2]
                                (if (contains? m1 k2)
                                  (assoc m1 k2 (flatten [v2 (k2 m1)])))))))

(defmacro for* [[cursor coll] & loop-body]
  `(remove nil? (for [~cursor ~coll]
                  (do
                    ~@loop-body))))

(def os-diagnostics (str "set -o xtrace; hostname -f;rolename -l;free -mh;dmesg -T | grep -i error | grep -v usb | wc -l ;dmesg -T | grep -i error | grep -i 'sd[a-z]' | tail -30 ;lsscsi | wc -l;lsscsi;sudo ethtool $(sudo facter -p default_interface) | grep -i speed;dmesg -T | grep -i error | tail -30;ifconfig -a;last -50;uptime | grep -v day;uptime;supervisorctl status;uname -r; supervisorctl status | grep -v day;dmesg -T | grep -i error | grep \"Hardware Error\" | tail -1000;df -kh; sudo cat /proc/mdstat;sudo cat /sys/block/sda/queue/rotational"))

(defn lsof []
  (let [c (me.raynes.conch.low-level/proc "lsof" "-iTCP" "-sTCP:LISTEN" "-P" "-n")]
    (future (me.raynes.conch.low-level/stream-to-out c :out))))

(defn ssh-tunnel [host port]
  (let [c (me.raynes.conch.low-level/proc "ssh" (str "-NL" port ":127.0.0.1:" port) host)]
    (future (me.raynes.conch.low-level/stream-to-out c :out))))

(defn clush
  ([host command]
   (let [c (me.raynes.conch.low-level/proc "clush" "-o" "\"-A\"" "-w" host "-B" command)
         output (me.raynes.conch.low-level/stream-to-out c :out)
         ]))
  ([host command pretty]
   (me.raynes.conch.low-level/stream-to-string (me.raynes.conch.low-level/proc "clush" "-o" "\"-A\"" "-w" host "-L" command) :out)))

(defn clush-lsof [host]
  (clush host "sudo lsof -iTCP -sTCP:LISTEN -P -n"))

(defn clush-sup-stat [host]
  (clush host "supervisorctl status"))

(defn clush-os-diagnostics [host]
  (clush host os-diagnostics))

(defn clush-dmesg [host]
  (clush host "dmesg -T | grep -i error | wc -l"))

(defmacro vxe-hotload-dependency [coordinates]
  (do
    (use '[cemerick.pomegranate :only (add-dependencies)])
    `(cemerick.pomegranate/add-dependencies :coordinates '[~coordinates]
                        :repositories (merge cemerick.pomegranate.aether/maven-central
                                             {"clojars" "https://clojars.org/repo"}))))
