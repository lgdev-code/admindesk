package fr.lgdev.admindesk.web;

import fr.lgdev.admindesk.domain.Priorite;
import fr.lgdev.admindesk.dto.DemandeFormDTO;
import fr.lgdev.admindesk.dto.PropositionTriage;
import fr.lgdev.admindesk.dto.TriageDemandeForm;
import fr.lgdev.admindesk.service.DemandeService;
import fr.lgdev.admindesk.service.agent.TriageAgentService;
import fr.lgdev.admindesk.service.agent.TriageTrace;
import fr.lgdev.admindesk.service.agent.TypeDemandeMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Agent de triage (bonus D5). Trois etapes :
 *  - GET  /agent           : le guichet saisit la demande du citoyen ;
 *  - POST /agent/analyser  : l'AGENT propose un routage + affiche sa trace d'outils ;
 *  - POST /agent/valider   : un HUMAIN valide -> creation deterministe de la Demande.
 */
@Controller
@RequestMapping("/agent")
@RequiredArgsConstructor
public class TriageViewController {

    private final TriageAgentService triageAgentService;
    private final DemandeService demandeService;
    private final TriageTrace trace;

    @GetMapping
    public String formulaire(Model model) {
        model.addAttribute("form", new TriageDemandeForm());
        return "agent/formulaire";
    }

    @PostMapping("/analyser")
    public String analyser(@Valid @ModelAttribute("form") TriageDemandeForm form,
                           BindingResult br, Model model) {
        if (br.hasErrors()) {
            return "agent/formulaire";
        }
        PropositionTriage proposition = triageAgentService.analyser(form.getTexte());
        model.addAttribute("proposition", proposition);
        model.addAttribute("trace", trace.getAppels()); // on revele les appels d'outils
        model.addAttribute("demande", form.getTexte());
        return "agent/proposition";
    }

    /**
     * Human-in-the-loop : l'effet de bord (creation de la Demande) a lieu UNIQUEMENT ici,
     * apres un clic explicite, et de facon deterministe via DemandeService.create(...).
     * Le LLM n'est plus dans la boucle a ce stade.
     */
    @PostMapping("/valider")
    public String valider(@RequestParam String typeDemande,
                          @RequestParam String priorite,
                          @RequestParam String description,
                          @RequestParam String nomDemandeur,
                          @RequestParam String emailDemandeur,
                          @RequestParam(required = false) String telephoneDemandeur,
                          RedirectAttributes flash) {

        var dto = new DemandeFormDTO();
        dto.setNomDemandeur(nomDemandeur);
        dto.setEmailDemandeur(emailDemandeur);
        dto.setTelephoneDemandeur(telephoneDemandeur);
        dto.setType(TypeDemandeMapper.from(typeDemande));
        dto.setDescription(description);
        dto.setPriorite(parsePriorite(priorite));

        var creee = demandeService.create(dto);
        flash.addFlashAttribute("success",
                "Demande %s creee par l'agent de triage.".formatted(creee.getReference()));
        return "redirect:/demandes/" + creee.getId();
    }

    private Priorite parsePriorite(String raw) {
        if (raw == null) {
            return Priorite.NORMALE;
        }
        try {
            return Priorite.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return Priorite.NORMALE;
        }
    }
}
