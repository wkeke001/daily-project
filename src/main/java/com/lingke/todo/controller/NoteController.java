package com.lingke.todo.controller;

import com.lingke.todo.entity.Note;
import com.lingke.todo.repository.NoteRepository;
import com.lingke.todo.security.SecurityUtil;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/notes")
public class NoteController {

    private final NoteRepository noteRepository;

    public NoteController(NoteRepository noteRepository) {
        this.noteRepository = noteRepository;
    }

    @GetMapping
    public String index(Model model) {
        Long userId = SecurityUtil.getCurrentUserId();
        List<Note> allNotes = noteRepository.findByUserIdOrderByNoteDateDescCreatedAtDesc(userId);
        Map<LocalDate, List<Note>> dateNotes = allNotes.stream().collect(Collectors.groupingBy(
                Note::getNoteDate,
                LinkedHashMap::new,
                Collectors.toList()
        ));
        model.addAttribute("dateNotes", dateNotes);
        model.addAttribute("today", LocalDate.now());
        return "notes";
    }

    @PostMapping("/add")
    public String add(@RequestParam String content,
                      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate noteDate) {
        Long userId = SecurityUtil.getCurrentUserId();
        Note note = new Note();
        note.setContent(content);
        note.setNoteDate(noteDate);
        note.setUserId(userId);
        noteRepository.save(note);
        return "redirect:/notes";
    }

    @PostMapping("/update/{id}")
    public String update(@PathVariable Long id,
                         @RequestParam String content,
                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        Long userId = SecurityUtil.getCurrentUserId();
        Note note = noteRepository.findById(id).orElseThrow();
        if (!note.getUserId().equals(userId)) throw new IllegalStateException("No permission");
        note.setContent(content);
        noteRepository.save(note);
        return "redirect:/notes";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id,
                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        Long userId = SecurityUtil.getCurrentUserId();
        Note note = noteRepository.findById(id).orElseThrow();
        if (!note.getUserId().equals(userId)) throw new IllegalStateException("No permission");
        noteRepository.delete(note);
        return "redirect:/notes";
    }
}