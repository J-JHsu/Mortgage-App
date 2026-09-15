package edu.cs213.mortgage;

public class Mortgage {
    private int applicationId;
    private String respondentId;
    private int loanType;
    private int loanAmount;
    private int actionTaken;
    private int msamd;
    private int applicantIncome;
    private double rateSpread;      
    private int purchaserType;
    private int lienStatus;
    private int propertyType;
    private int loanPurpose;
    private int ownerOccupancy;

    public Mortgage(int applicationId, String respondentId, int loanType, int loanAmount,
                    int actionTaken, int msamd,
                    int applicantIncome, double rateSpread, int purchaserType, int lienStatus, 
                    int propertyType, int loanPurpose, int ownerOccupancy) {
        this.applicationId = applicationId;
        this.respondentId = respondentId;
        this.loanType = loanType;
        this.loanAmount = loanAmount;
        this.actionTaken = actionTaken;
        this.msamd = msamd;
        this.applicantIncome = applicantIncome;
        this.rateSpread = rateSpread;
        this.purchaserType = purchaserType;
        this.lienStatus = lienStatus;
        this.propertyType = propertyType;
        this.loanPurpose = loanPurpose;
        this.ownerOccupancy = ownerOccupancy;
    }

    // Getter for lienStatus
    public int getLienStatus() {
        return lienStatus;
    }

    // Other existing getters (if needed)
    public int getApplicationId() { return applicationId; }
    public String getRespondentId() { return respondentId; }
    public int getLoanType() { return loanType; }
    public int getLoanAmount() { return loanAmount; }
    public int getActionTaken() { return actionTaken; }
    public int getMsamd() { return msamd; }
    public int getApplicantIncome() { return applicantIncome; }
    public double getRateSpread() { return rateSpread; }
    public int getPurchaserType() { return purchaserType; }
    public int getOwnerOccupancy() { return ownerOccupancy; }
}
