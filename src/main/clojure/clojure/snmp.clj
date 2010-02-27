(ns
  #^{:author "Jorge Falcão",
     :doc "SNMP Client"}
  clojure.snmp
  (:import
    [org.snmp4j Snmp PDU CommunityTarget]
    [org.snmp4j.mp SnmpConstants]
    [org.snmp4j.transport DefaultUdpTransportMapping]
    [org.snmp4j.event.ResponseEvent]
    [org.snmp4j.smi OID VariableBinding OctetString GenericAddress]
    [java.net InetAddress]
    )
  )

(defn build-target [host community]
  "Returns CommunityTarget object"
  (doto (CommunityTarget.)
    (.setCommunity (new OctetString community))
    (.setAddress (GenericAddress/parse (format "udp:%s/161" host)))
    (.setVersion SnmpConstants/version1)
    (.setRetries 0)
    (.setTimeout 1000)
    )
  )

;(println (str "@" (build-target "host" "public")))

(defn build-pdu [oid]
  "Build PDU Object"
  (doto (PDU.)
    (.setType PDU/GETBULK)
    (.addAll (into-array (map #(VariableBinding. (OID. (str %))) oid)))
    )
  )

;(build-pdu '("1.3.6.1.2.1.1.3.0" "1.3.6.1.2.1.2.2.1.21.1"))

(defn response? [response]
  (and response (== (. response getType) PDU/RESPONSE))
  )

;(response? nil)

(defn snmpgetv1 [host community & oid]
  (do
    (def pdu (build-pdu oid))
    (def target (build-target host community))
    (def snmp (Snmp. (DefaultUdpTransportMapping.)))
    (. snmp listen)
    (def event (. snmp get pdu target))
    (def response (. event getResponse))
    (. snmp close)
    (when (response? response)
      ; return data.
      (map #(str (. % getVariable)) (. response getVariableBindings))
    )
  )
)

(println (str "sysUpTime = " (snmpgetv1 "192.168.0.3" "public" "1.3.6.1.2.1.1.3.0" "1.3.6.1.2.1.2.2.1.21.1")))

