package ru.practicum.compilation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.dto.UpdateCompilationRequest;
import ru.practicum.compilation.mapper.CompilationMapper;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.compilation.repository.CompilationRepository;
import ru.practicum.compilation.service.interfaces.AdminCompilationsService;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.NotFoundException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminCompilationsServiceImpl implements AdminCompilationsService {

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;

    @Override
    public CompilationDto createCompilation(NewCompilationDto newCompilationDto) {
        List<Event> events = eventRepository.findByIdIn(newCompilationDto.getEvents());
        Compilation compilation = CompilationMapper.toCompilation(newCompilationDto, events);
        List<EventShortDto> eventShortDtos = events.stream()
                .map(EventMapper::toEventShortDto)
                .toList();

        return CompilationMapper.toCompilationDto(compilationRepository.save(compilation), eventShortDtos);
    }

    @Override
    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest updateCompilationRequest) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Подборка с id = " + compId + " не найдена."));
        List<Event> events = eventRepository.findByIdIn(updateCompilationRequest.getEvents());

        if (updateCompilationRequest.getEvents() != null) {
            compilation.setEvents(events);
        }
        if (updateCompilationRequest.getTitle() != null) {
            compilation.setTitle(updateCompilationRequest.getTitle());
        }
        if (updateCompilationRequest.getPinned() != null) {
            compilation.setPinned(updateCompilationRequest.getPinned());
        }
        List<EventShortDto> eventShortDtos = events.stream()
                .map(EventMapper::toEventShortDto)
                .toList();

        return CompilationMapper.toCompilationDto(compilation, eventShortDtos);
    }

    @Override
    public void deleteCompilation(Long compId) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Подборка с id = " + compId + " не найдена."));
        compilationRepository.deleteById(compId);
    }
}