package data;

import com.fasterxml.jackson.annotation.JsonProperty;
import data.annotations.OnPage;
import data.annotations.RequiredOnPage;
import data.base.ReportGeneratorWizardData;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class ParoleParom1ReportData extends ReportGeneratorWizardData {

    @RequiredOnPage(value = 3, message = "Enter how long you have managed the prisoner, and what contact you have had with them")
    @JsonProperty("PRISONER_CONTACT_DETAIL")
    private String prisonerContactDetail;

    @RequiredOnPage(value = 3, message = "Enter what contact you have had with the prisoner's family, partners or significant others")
    @JsonProperty("PRISONER_CONTACT_FAMILY_DETAIL")
    private String prisonerContactFamilyDetail;

    @RequiredOnPage(value = 3, message = "Enter what contact you have had with other relevant agencies about the prisoner")
    @JsonProperty("PRISONER_CONTACT_AGENCIES_DETAIL")
    private String prisonerContactAgenciesDetail;

    @RequiredOnPage(value = 6, message = "Specify if the prisoner has met OPD screening criteria and been considered for OPD pathway services")
    @JsonProperty("CONSIDERED_FOR_OPD_PATHWAY_SERVICES")
    private String consideredForOPDPathwayServices;

    // Page 7 - Behaviour in prison
    @RequiredOnPage(value = 7, message = "Enter details of the prisoner’s behaviour in prison")
    @JsonProperty("BEHAVIOUR_DETAIL")
    private String behaviourDetail;

    @RequiredOnPage(value = 7, message = "Enter the RoTL summary")
    @JsonProperty("ROTL_SUMMARY")
    private String rotlSummary;

    @RequiredOnPage(value = 8, message = "Detail the interventions the prisoner has completed")
    @JsonProperty("INTERVENTIONS_DETAIL")
    private String interventionsDetail;

    @RequiredOnPage(value = 8, message = "Enter the interventions summary")
    @JsonProperty("INTERVENTIONS_SUMMARY")
    private String interventionsSummary;

    // Page 9 - Current sentence plan and response
    @RequiredOnPage(value = 9, message = "Enter the prisoner's current sentence plan objectives, including their response")
    @JsonProperty("SENTENCE_PLAN")
    private String sentencePlan;

    // Page 23
    @OnPage(23)
    private String dummy23;

}
