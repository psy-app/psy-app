package com.psy;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/* 
  Колекція, в якій зберігатися документ баз даних MongoDB, що представляє сутність рядку з таблиці.
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

    public PSession( String psychologistName, String clientName,
         String sessionDate, String sessionPackage, String sessionTopic,
          String MeetingPlatform, String subscription, String psyExperience,
           String clinicAddress, String clinicPhone) {
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
    
    public String getPsychologistName() {
        return psychologistName;
    }

    public void setPsychologistName(String psychologistName) {
        this.psychologistName = psychologistName;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getSessionDate() {
        return sessionDate;
    }

    public void setSessionDate(String sessionDate) {
        this.sessionDate = sessionDate;
    }

    public String getSessionPackage() {
        return sessionPackage;
    }

    public void setSessionPackage(String sessionPackage) {
        this.sessionPackage = sessionPackage;
    }

    public String getSessionTopic() {
        return sessionTopic;
    }

    public void setSessionTopic(String sessionTopic) {
        this.sessionTopic = sessionTopic;
    }

    public String getMeetingPlatform() {
        return MeetingPlatform;
    }

    public void setMeetingPlatform(String MeetingPlatform) {
        this.MeetingPlatform = MeetingPlatform;
    }

    public String getSubscription() {
        return subscription;
    }

    public void setSubscription(String subscription) {
        this.subscription = subscription;
    }

    public String getPsyExperience() {
        return psyExperience;
    }

    public void setPsyExperience(String psyExperience) {
        this.psyExperience = psyExperience;
    }

    public String getClinicAddress() {
        return clinicAddress;
    }

    public void setClinicAddress(String clinicAddress) {
        this.clinicAddress = clinicAddress;
    }

    public String getClinicPhone() {
        return clinicPhone;
    }

    public void setClinicPhone(String clinicPhone) {
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
