package com.psy;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/* 
  Колекція, в якій зберігатися документ баз даних MongoDB, що представляє сутність рядку з таблиці з розкладом коледжу.
*/
@Document(collection = "psy-collection")
public class PSession {
    @Id
    private String id;
    private String clientName;
    private String psychologistName;
    private String sessionPackage;
    private String sessionTopic;
    private String MeetingPlatform;
    private String subscription;
    private String psyExperience;
    private String clinicAddress;
    private String clinicPhone;
    private String sessionDate;


    public PSession( String psychologistName, String clientName, String sessionDate, String sessionPackage, String sessionTopic, String MeetingPlatform, String subscription, String psyExperience, String clinicAddress, String clinicPhone) {
        
        
        this.psychologistName = psychologistName;
        this.clientName = clientName;
        this.sessionDate = sessionDate;
        this.sessionPackage = sessionPackage;
        this.sessionTopic = sessionTopic;
        this.MeetingPlatform = MeetingPlatform;
        this.subscription = subscription;
        this.psyExperience = psyExperience;
        this.clinicAddress = clinicAddress;
        this.clinicPhone = clinicPhone;
        

    }

    public String toString() {
        return "PSession {" +
                " id=\"" + id + "\"\n" +
                " clientName=\"" + clientName + "\"\n" +
                " psychologistName=\"" + psychologistName + "\"\n" +
                " sessionPackage=\"" + sessionPackage + "\"\n" +
                " sessionTopic=\"" + sessionTopic + "\"\n" +
                " MeetingPlatform=\"" + MeetingPlatform + "\"\n" +
                " subscription=\"" + subscription + "\"\n" +
                " psyExperience=\"" + psyExperience + "\"\n" +
                " clinicAddress=\"" + clinicAddress + "\"\n" +
                " clinicPhone=\"" + clinicPhone + "\"\n" +
                " sessionDate=\"" + sessionDate + "\"\n" +
                "}";
    }
}