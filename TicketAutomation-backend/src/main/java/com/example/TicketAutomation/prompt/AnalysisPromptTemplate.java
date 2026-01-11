package com.example.TicketAutomation.prompt;

public class AnalysisPromptTemplate {
    private AnalysisPromptTemplate(){}

    public static String build(String title , String description){
        return """
                YOU ARE A PROFESSIONAL QUERY TICKET RESOLVER SYSTEM.
               \s
                ANALYZE THE FOLLOWING INPUT AND RETURN A STRUCTURED JSON RESPONSE.
               \s
                RULES :
              1 : WHEN ASKED TO PREDICT CATEGORY  OF THE TICKET ONLY ALLOWED OPTIONS ARE ONLY
                  ONE  AMONG THESE { BILLING, TECHNICAL,ACCOUNT,SECURITY, OTHER } NO OTHER CATEGORY
                  IS NOT  ALLOWED TO RETURN
                 \s
              2 : WHEN ASKED TO PREDICT THE PRIORITY OF THE TICKET  ONLY ALLOWED OPTIONS ARE ONLY
                  ONE AMONG THESE { LOW,MEDIUM,HIGH,CRITICAL} NO OTHER PRIORITY IS ALLOWED TO RETURN
                 \s
              3 : RETURN THE SUMMARY OF THE QUERY DESCRIPTION OR CONVERSATION BETWEEN USER AND AGENT      \s
              
              4 : SUMMARY RESPONSE MUST BE BETWEEN 80 - 130 WORDS NOT LESS NO MORE      \s
               \s
              Input:
              Title : %s
              Description : %s
               \s""".formatted(title,description);
    }
}
