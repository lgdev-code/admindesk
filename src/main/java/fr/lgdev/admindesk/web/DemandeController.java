package fr.lgdev.admindesk.web;

import fr.lgdev.admindesk.domain.Priorite;
import fr.lgdev.admindesk.domain.StatutDemande;
import fr.lgdev.admindesk.domain.TypeDemande;
import fr.lgdev.admindesk.dto.DemandeFormDTO;
import fr.lgdev.admindesk.service.DemandeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/demandes")
@RequiredArgsConstructor
public class DemandeController {

    private final DemandeService service;

    @GetMapping
    public String list(
            @RequestParam(required = false) StatutDemande statut,
            @RequestParam(required = false) TypeDemande   type,
            @RequestParam(required = false) Priorite      priorite,
            @RequestParam(required = false) String        search,
            @RequestParam(defaultValue = "0") int         page,
            Model model) {

        model.addAttribute("demandes",       service.search(statut, type, priorite, search, page));
        model.addAttribute("statuts",        StatutDemande.values());
        model.addAttribute("types",          TypeDemande.values());
        model.addAttribute("priorites",      Priorite.values());
        model.addAttribute("filtreStatut",   statut);
        model.addAttribute("filtreType",     type);
        model.addAttribute("filtrePriorite", priorite);
        model.addAttribute("search",         search);
        return "demandes/list";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("total",         service.count());
        model.addAttribute("statsStatut",   service.statsByStatus());
        model.addAttribute("statsType",     service.statsByType());
        model.addAttribute("statsPriorite", service.statsByPriority());
        return "demandes/dashboard";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("demande", service.findById(id));
        return "demandes/detail";
    }

    @GetMapping("/nouveau")
    public String newForm(Model model) {
        model.addAttribute("dto",         new DemandeFormDTO());
        model.addAttribute("types",       TypeDemande.values());
        model.addAttribute("statuts",     StatutDemande.values());
        model.addAttribute("priorites",   Priorite.values());
        model.addAttribute("modeEdition", false);
        return "demandes/form";
    }

    @PostMapping("/nouveau")
    public String create(@Valid @ModelAttribute("dto") DemandeFormDTO dto,
                         BindingResult br, Model model, RedirectAttributes flash) {
        if (br.hasErrors()) {
            model.addAttribute("types",       TypeDemande.values());
            model.addAttribute("statuts",     StatutDemande.values());
            model.addAttribute("priorites",   Priorite.values());
            model.addAttribute("modeEdition", false);
            return "demandes/form";
        }
        var d = service.create(dto);
        flash.addFlashAttribute("success", "Demande %s créée avec succès.".formatted(d.getReference()));
        return "redirect:/demandes/" + d.getId();
    }

    @GetMapping("/{id}/modifier")
    public String editForm(@PathVariable Long id, Model model) {
        var d   = service.findById(id);
        var dto = new DemandeFormDTO();
        dto.setNomDemandeur(d.getNomDemandeur());
        dto.setEmailDemandeur(d.getEmailDemandeur());
        dto.setTelephoneDemandeur(d.getTelephoneDemandeur());
        dto.setType(d.getType());
        dto.setDescription(d.getDescription());
        dto.setStatut(d.getStatut());
        dto.setPriorite(d.getPriorite());
        dto.setAgentTraitant(d.getAgentTraitant());
        dto.setCommentaireAgent(d.getCommentaireAgent());
        model.addAttribute("dto",         dto);
        model.addAttribute("demande",     d);
        model.addAttribute("types",       TypeDemande.values());
        model.addAttribute("statuts",     StatutDemande.values());
        model.addAttribute("priorites",   Priorite.values());
        model.addAttribute("modeEdition", true);
        return "demandes/form";
    }

    @PostMapping("/{id}/modifier")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("dto") DemandeFormDTO dto,
                         BindingResult br, Model model, RedirectAttributes flash) {
        if (br.hasErrors()) {
            model.addAttribute("demande",     service.findById(id));
            model.addAttribute("types",       TypeDemande.values());
            model.addAttribute("statuts",     StatutDemande.values());
            model.addAttribute("priorites",   Priorite.values());
            model.addAttribute("modeEdition", true);
            return "demandes/form";
        }
        service.update(id, dto);
        flash.addFlashAttribute("success", "Demande mise à jour avec succès.");
        return "redirect:/demandes/" + id;
    }

    @PostMapping("/{id}/supprimer")
    public String delete(@PathVariable Long id, RedirectAttributes flash) {
        service.delete(id);
        flash.addFlashAttribute("success", "Demande supprimée.");
        return "redirect:/demandes";
    }

    @PostMapping("/{id}/resumer")
    public String resumer(@PathVariable Long id) {
        service.summarize(id);
        return "redirect:/demandes/" + id;
    }
}
