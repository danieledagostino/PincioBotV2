package com.pincio.telegramwebhook.controller;

import com.pincio.telegramwebhook.model.Question;
import com.pincio.telegramwebhook.service.AIResponseService;
import com.pincio.telegramwebhook.service.MaskReplacementService;
import com.pincio.telegramwebhook.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class AdminController {

    @Autowired
    private QuestionService questionService;

    @Autowired
    private AIResponseService aiResponseService;

    @Autowired
    MaskReplacementService maskReplacementService;

    @GetMapping("/questions")
    public String viewQuestionsToReview(Model model) {
        Iterable<Question> questions = questionService.getAllQuestions();
        model.addAttribute("questions", questions);
        return "questions";
    }

    @PostMapping("/admin/questions/manual-mask")
    public String manuallyMaskQuestion(@RequestParam("questionId") String questionId,
                                       @RequestParam("maskedQuestion") String maskedQuestion,
                                       Model model) {
        // Recupera la domanda originale
        Optional<Question> originalQuestionOpt = questionService.getQuestionById(questionId);
        String originalQuestion = "";
        if (originalQuestionOpt.isPresent()) {
            originalQuestion = originalQuestionOpt.get().getQuestionText();
        }
        // Aggiorna lo storico delle sostituzioni
        questionService.updateReplacementHistory(originalQuestion, maskedQuestion);

        // Salva la domanda mascherata
        questionService.saveMaskedQuestion(maskedQuestion);

        model.addAttribute("message", "Masked question saved successfully.");
        return "manual_mask_success";
    }

    // Mostra la pagina per modificare una domanda
    @GetMapping("/edit-question")
    public String editQuestionPage(@RequestParam String id, Model model) {
        Optional<Question> question = questionService.getQuestionById(id);
        if (question.isPresent()) {
            model.addAttribute("question", question.get());
            return "edit-question";
        } else {
            model.addAttribute("error", "Question not found!");
            return "error";
        }
    }

    // Salva una domanda modificata
    @PostMapping("/edit-question")
    public String updateQuestion(
            @RequestParam String id,
            @RequestParam String updatedText,
            Model model) {
        Optional<Question> questionOpt = questionService.getQuestionById(id);
        if (questionOpt.isPresent()) {
            if (!updatedText.contains("[MASK]")) {
                // Mostra un messaggio di errore se manca il token [MASK]
                model.addAttribute("error", "The updated question must contain the [MASK] token!");
                model.addAttribute("question", questionOpt.get());
                return "edit-question"; // Ritorna alla pagina di modifica
            }

            // Aggiorna la domanda solo se è valido
            Question question = questionOpt.get();
            question.setQuestionText(updatedText);
            aiResponseService.callHuggingFaceApiWithMask(updatedText); // Richiama l'API Hugging Face per aggiornare la risposta
            question.setSentToHuggingFace(true);
            questionService.updateQuestion(question); // Salva la domanda aggiornata
            model.addAttribute("success", "Question updated successfully!");
            return "redirect:/questions";
        } else {
            model.addAttribute("error", "Question not found!");
            return "error";
        }
    }

    @GetMapping("/delete-question")
    public String deleteQuestion(@RequestParam String id, RedirectAttributes redirectAttributes) {
        Optional<Question> questionOpt = questionService.getQuestionById(id);
        if (questionOpt.isPresent()) {
            questionService.deleteQuestion(id);
            redirectAttributes.addFlashAttribute("success", "Question deleted successfully!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Question not found!");
        }
        return "redirect:/questions";
    }

    @GetMapping("/admin/questions/{id}")
    public String getQuestionDetails(@PathVariable String id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Question> questionOpt = questionService.getQuestionById(id);
        if (questionOpt.isPresent()) {
            model.addAttribute("question", questionOpt.get());
            return "question-details";
        } else {
            redirectAttributes.addFlashAttribute("error", "Question not found!");
            return "redirect:/questions";
        }
    }

    @PostMapping("/admin/questions/approve")
    public String approveAnswer(@RequestParam String questionId, @RequestParam String answer, RedirectAttributes redirectAttributes) {
        Optional<Question> questionOpt = questionService.getQuestionById(questionId);

        if (questionOpt.isPresent()) {
            Question question = questionOpt.get();

            // Imposta la risposta confermata
            question.setConfirmedAnswer(answer);
            question.setConfirmed(true);
            questionService.updateQuestion(question);

            redirectAttributes.addFlashAttribute("success", "Answer approved successfully for the question!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Question not found!");
        }

        return "redirect:/questions";
    }
}
