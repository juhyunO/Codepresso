package com.codepresso.codepresso.board.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 게시판 뷰 컨트롤러
 */
@Controller
@RequestMapping("/boards")
public class BoardViewController {

    @GetMapping("/list")
    public String boardList() {
        return "board/board-list";
    }

    @GetMapping("/detail/{boardId}")
    public String boardDetail(@PathVariable Long boardId, Model model) {
        model.addAttribute("boardId", boardId);
        return "board/board-detail";
    }

    @GetMapping("/write")
    public String boardWrite() {
        return "board/board-write";
    }

    @GetMapping("/edit/{boardId}")
    public String boardEdit(@PathVariable Long boardId, Model model) {
        model.addAttribute("boardId", boardId);
        return "board/board-write";
    }
}
