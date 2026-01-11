package com.example.TicketAutomation.service;

import com.example.TicketAutomation.Repository.AiMetaRepository;
import com.example.TicketAutomation.Repository.TicketRepository;
import com.example.TicketAutomation.dto.AnalysisResponse;
import com.example.TicketAutomation.dto.MessageResponse;
import com.example.TicketAutomation.dto.TicketDetailResponse;
import com.example.TicketAutomation.exception.NoSuchTicketIdExist;
import com.example.TicketAutomation.model.AiMetaData;
import com.example.TicketAutomation.model.Category;
import com.example.TicketAutomation.model.Ticket;
import com.example.TicketAutomation.model.TicketStatus;
import com.example.TicketAutomation.prompt.AnalysisPromptTemplate;
import com.example.TicketAutomation.prompt.DeepAnalysisPromptTemplate;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Service
public class AiServices {
    private final TicketRepository ticketRepository;
    private final AiMetaRepository aiMetaRepository;
    private final OllamaChatModel chatModel;
    private final BeanOutputConverter<AnalysisResponse> converter;
    private final MessageService messageService;



    public AiServices(TicketRepository ticketRepository,AiMetaRepository aiMetaRepository,OllamaChatModel chatModel,MessageService messageService){
        this.ticketRepository = ticketRepository;
        this.aiMetaRepository = aiMetaRepository;
        this.chatModel = chatModel;
        this.converter = new BeanOutputConverter<>(AnalysisResponse.class);
        this.messageService = messageService;
    }




    @Async
    public void analyzeOpenTicket(String ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(()->new NoSuchTicketIdExist("No such Ticket ID Exist : "));

        if(!ticket.getStatus().equals(TicketStatus.OPEN)){
            throw new IllegalStateException("Not Allowed to Use AI analysis");
        }

        String title =  ticket.getTitle();
        String description = ticket.getDescription();

        String promptText = AnalysisPromptTemplate.build(title,description);

        Prompt prompt = new Prompt(
                promptText,
                OllamaChatOptions.builder()
                        .format(converter.getJsonSchemaMap())
                        .build()
        );

        AnalysisResponse analysisResponse;
        try{
            ChatResponse response = chatModel.call(prompt);
            String json  = response.getResult().getOutput().getText();
             analysisResponse =   converter.convert(json);
        }catch (Exception e){
            throw new RuntimeException("Error Connecting to AI :  ");
        }

        AiMetaData data = aiMetaRepository.findByTicketId(ticketId);

           if(data == null){
               AiMetaData aiMetaData = AiMetaData.builder()
                       .ticket(ticket)
                       .summary(analysisResponse.getSummary())
                       .predictedPriority(analysisResponse.getPredictedPriority())
                       .predicatedCategory(analysisResponse.getPredictedCategory())
                       .createdAt(Instant.now())
                       .locked(false)
                       .build();
               aiMetaRepository.save(aiMetaData);

           }else{
               if(!data.isLocked()) {
                   data.setSummary(analysisResponse.getSummary());
                   data.setPredicatedCategory(analysisResponse.getPredictedCategory());
                   data.setPredictedPriority(analysisResponse.getPredictedPriority());
                   aiMetaRepository.save(data);
               }
           }
    }

    @Transactional
    @Async
    public void analyzeWithChat(String ticketId) {

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() ->
                        new NoSuchTicketIdExist("No such Ticket exist: " + ticketId));

        List<MessageResponse> conversationList =
                messageService.aiThroughputChat(ticketId);

        String conversation = buildConversation(conversationList);

        // ✅ Skip AI if no meaningful conversation
        if (conversation == null) {
            throw  new RuntimeException("No need to generate the response : ");
        }

        String promptText = DeepAnalysisPromptTemplate.build(
                ticket.getTitle(),
                ticket.getDescription(),
                conversation
        );

        Prompt prompt = new Prompt(
                promptText,
                OllamaChatOptions.builder()
                        .format(converter.getJsonSchemaMap())
                        .build()
        );

        ChatResponse response;
        try {
            response = chatModel.call(prompt);
        } catch (Exception e) {
            throw new RuntimeException("Error Connecting to AI", e);
        }

        String json = response.getResult().getOutput().getText();
        AnalysisResponse analysisResponse = converter.convert(json);

        AiMetaData aiMetaData = aiMetaRepository.findByTicketId(ticketId);

        // ✅ Null-safe metadata handling
        if (aiMetaData != null && !aiMetaData.isLocked()) {
            aiMetaData.setPredictedPriority(analysisResponse.getPredictedPriority());
            aiMetaData.setPredicatedCategory(analysisResponse.getPredictedCategory());
            aiMetaData.setSummary(analysisResponse.getSummary());

            aiMetaRepository.save(aiMetaData);
        }

    }


    public String buildConversation(List<MessageResponse> conversation) {

        if (conversation == null || conversation.isEmpty()) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        int meaningfulCount = 0;

        for (MessageResponse msg : conversation) {
            if (!isMeaningfulMessage(msg.getMessage())) {
                continue;
            }

            sb.append(msg.getSenderType())
                    .append(": ")
                    .append(msg.getMessage())
                    .append("\n");

            meaningfulCount++;
        }

        return meaningfulCount == 0 ? null : sb.toString();
    }

    public static final Set<String> SMALL_TALK = Set.of(
            "ok", "okay", "thanks", "thank you", "thx",
            "fine", "sure", "cool", "got it", "alright",
            "hello", "yes", "no"
    );

    private boolean isMeaningfulMessage(String text) {
        if (text == null) return false;

        String normalized = text.trim().toLowerCase();

        if (normalized.length() < 10) return false;

        if (SMALL_TALK.contains(normalized)) return false;

        return normalized.contains(" ");
    }


    public AnalysisResponse getSummary(String id, String ticketId) {
       Ticket ticket =  ticketRepository.findById(ticketId).orElseThrow(()-> new NoSuchTicketIdExist("No Such ticketId exist:"));
       if(ticket.getCreatedBy().getId().equals(id) || ticket.getAssignedTo().getId().equals(id)){
           AiMetaData getData =  aiMetaRepository.findByTicketId(ticketId);
           return new AnalysisResponse(
                   getData.getPredicatedCategory(),
                   getData.getPredictedPriority(),
                   getData.getSummary()
           );
       }
       throw new RuntimeException("Not allowed to view the summary ! ");
    }
}
