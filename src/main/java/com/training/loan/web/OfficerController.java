package com.training.loan.web;

import com.training.loan.domain.LoanApplication;
import com.training.loan.domain.enums.ApplicationStatus;
import com.training.loan.service.EvaluationPipeline;
import com.training.loan.service.LoanApplicationService;
import java.security.Principal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/officer")
public class OfficerController {

    private final LoanApplicationService applicationService;
    private final EvaluationPipeline evaluationPipeline;

    public OfficerController(LoanApplicationService applicationService,
                             EvaluationPipeline evaluationPipeline) {
        this.applicationService = applicationService;
        this.evaluationPipeline = evaluationPipeline;
    }

    @GetMapping("/queue")
    public String queue(Model model) {
        model.addAttribute("applications", applicationService.manualReviewQueue());
        return "officer/queue";
    }

    @GetMapping("/applications/{id}")
    public String review(@PathVariable Long id, Model model) {
        LoanApplication app = applicationService.get(id);
        model.addAttribute("app", app);
        model.addAttribute("communications", applicationService.communicationsFor(id));
        return "officer/review";
    }

    @PostMapping("/applications/{id}/decision")
    public String decide(@PathVariable Long id,
                         @RequestParam("decision") ApplicationStatus decision,
                         @RequestParam(value = "notes", required = false) String notes,
                         Principal principal, RedirectAttributes redirect) {
        LoanApplication app = applicationService.get(id);
        evaluationPipeline.officerDecision(app, decision, principal.getName(), notes);
        redirect.addFlashAttribute("message", "Decision recorded: " + decision);
        return "redirect:/officer/applications/" + id;
    }
}
