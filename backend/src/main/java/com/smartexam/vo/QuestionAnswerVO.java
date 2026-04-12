package com.smartexam.vo;

import com.smartexam.entity.AnswerRecord;
import com.smartexam.entity.Question;
import lombok.Data;

@Data
public class QuestionAnswerVO {
    private Question question;
    private AnswerRecord answer;
    
    public QuestionAnswerVO(Question question, AnswerRecord answer) {
        this.question = question;
        this.answer = answer;
    }
}
