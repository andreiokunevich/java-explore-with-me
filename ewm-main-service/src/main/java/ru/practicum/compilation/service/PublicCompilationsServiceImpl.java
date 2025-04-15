package ru.practicum.compilation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.mapper.CompilationMapper;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.compilation.repository.CompilationRepository;
import ru.practicum.compilation.service.interfaces.PublicCompilationsService;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.Event;
import ru.practicum.exception.NotFoundException;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PublicCompilationsServiceImpl implements PublicCompilationsService {

    private final CompilationRepository compilationRepository;

    @Override
    public List<CompilationDto> getAllCompilations(Boolean pinned, int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size,Sort.by("id").ascending());
        List<Compilation> compilations;
        if (pinned != null) {
            compilations = compilationRepository.findByPinned(pinned, pageable).getContent();
        } else {
            compilations = compilationRepository.findAll(pageable).getContent();
        }

        if (compilations.isEmpty()) {
            return new ArrayList<>();
        }

        List<CompilationDto> compilationDtoList = new ArrayList<>();
        for (Compilation compilation : compilations) {
            List<Event> events = compilation.getEvents();
            List<EventShortDto> shortDtos = events.stream()
                    .map(EventMapper::toEventShortDto)
                    .toList();
            CompilationDto compilationDto = CompilationMapper.toCompilationDto(compilation, shortDtos);
            compilationDtoList.add(compilationDto);
        }

        return compilationDtoList;
    }

    @Override
    public CompilationDto getCompilationById(Long compId) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Подборка с id = " + compId + " не найдена."));
        List<Event> events = compilation.getEvents();
        List<EventShortDto> shortDtos = events.stream()
                .map(EventMapper::toEventShortDto)
                .toList();

        return CompilationMapper.toCompilationDto(compilation, shortDtos);
    }
}