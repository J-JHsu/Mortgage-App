package edu.cs213.mortgage.model;

public class Mortgage {
    private int applicationId;
    private String respondentId;
    private int loanType;
    private Integer loanAmount;
    private int actionTaken;
    private Integer msamd;
    private Integer applicantIncome;
    private Double rateSpread;
    private int purchaserType;
    private int lienStatus;
    private int propertyType;
    private int loanPurpose;
    private int ownerOccupancy;
    private Integer countyCode;
    private String censusTractNumber;
    private java.math.BigDecimal tractToMsamdIncome;

    public Mortgage(int applicationId, String respondentId, int loanType, Integer loanAmount,
                    int actionTaken, Integer msamd,
                    Integer applicantIncome, Double rateSpread, int purchaserType, int lienStatus,
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

    public Mortgage(int applicationId, String respondentId, int loanType, Integer loanAmount,
                    int actionTaken, Integer msamd, Integer applicantIncome, Double rateSpread,
                    int purchaserType, int lienStatus, int propertyType, int loanPurpose,
                    int ownerOccupancy, Integer countyCode, String censusTractNumber,
                    java.math.BigDecimal tractToMsamdIncome) {
        this(applicationId, respondentId, loanType, loanAmount, actionTaken, msamd,
                applicantIncome, rateSpread, purchaserType, lienStatus, propertyType, loanPurpose,
                ownerOccupancy);
        this.countyCode = countyCode;
        this.censusTractNumber = censusTractNumber;
        this.tractToMsamdIncome = tractToMsamdIncome;
    }

    public Integer getCountyCode() { return countyCode; }
    public String getCensusTractNumber() { return censusTractNumber; }
    public java.math.BigDecimal getTractToMsamdIncome() { return tractToMsamdIncome; }
    public int getPropertyType() { return propertyType; }
    public int getLoanPurpose() { return loanPurpose; }

    public int getLienStatus() {
        return lienStatus;
    }

    // Other existing getters (if needed)
    public int getApplicationId() { return applicationId; }
    public String getRespondentId() { return respondentId; }
    public int getLoanType() { return loanType; }
    public Integer getLoanAmount() { return loanAmount; }
    public int getActionTaken() { return actionTaken; }
    public Integer getMsamd() { return msamd; }
    public Integer getApplicantIncome() { return applicantIncome; }
    public Double getRateSpread() { return rateSpread; }
    public int getPurchaserType() { return purchaserType; }
    public int getOwnerOccupancy() { return ownerOccupancy; }
}
