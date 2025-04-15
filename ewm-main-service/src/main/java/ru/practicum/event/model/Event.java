package ru.practicum.event.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.category.model.Category;
import ru.practicum.event.model.enums.State;
import ru.practicum.user.model.User;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "annotation")
    private String annotation;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(name = "confirmed_requests")
    private Integer confirmedRequests;

    @Column(name = "created_on")
    private LocalDateTime createdOn;

    @NotBlank
    @Column(name = "description")
    private String description;

    @NotNull
    @Column(name = "event_date")
    private LocalDateTime eventDate;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "initiator_id")
    private User initiator;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private Location location;

    @Column(name = "participant_limit")
    private Integer participantLimit;

    @NotNull
    @Column(name = "paid")
    private Boolean paid;

    @Column(name = "published_on")
    private LocalDateTime publishedOn;

    @NotNull
    @Column(name = "request_moderation")
    private Boolean requestModeration;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "state")
    private State state;

    @NotBlank
    @Column(name = "title")
    private String title;

    @Transient
    private Integer views;
}