package nz.ac.auckland.concert.service.domain.jpa;

import nz.ac.auckland.concert.common.dto.NewsItemDTO;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Entity
public class NewsItem {

    @Id
    private Long id;

    private LocalDateTime timeStamp;

    private String content;

    public NewsItem(){}

    public NewsItem(Long id, LocalDateTime timeStamp, String content){
        this.id = id;
        this.timeStamp = timeStamp;
        this.content = content;
    }

    public NewsItemDTO convertToDTO(){
        return new NewsItemDTO(this.id, this.timeStamp, this.content);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(LocalDateTime timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
