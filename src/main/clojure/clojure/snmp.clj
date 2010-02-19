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
    )
  )

(defn build-target [host community]
  (let [target (CommunityTarget.)]
    (. target setCommunity (new OctetString community))
    (. target setAddress (GenericAddress/parse (format "udp:%s/161" host)))
    (. target setVersion SnmpConstants/version1)
    (. target setRetries 0)
    (. target setTimeout 1000)
    ; how improve?
    (first [target])
    )
  )

(defn snmpgetv1 [host community & oid]
  (let [pdu (PDU.)]
    (do
      ; add many
      (. pdu add (new VariableBinding (new OID (first oid))))
      (. pdu setType PDU/GETBULK)
      (def target (build-target host community))
      (def snmp (new Snmp (new DefaultUdpTransportMapping)))
      (. snmp listen)
      (def event (. snmp get pdu target))
      (def response (. event getResponse))
      (. snmp close)
      ;      (println (str "getType=" (. response getType)))
;      (if response ; response == nil
        (if (== (. response getType) PDU/RESPONSE)
          (str (. (first (. response getVariableBindings)) getVariable))
          nil
          )
        )
;      nil)
    ))

;1.3.6.1.2.1.2.2.1.21.1
;(println (str "sysUpTime = " (snmpgetv1 "192.168.0.3" "public" "1.3.6.1.2.1.1.3.0")))