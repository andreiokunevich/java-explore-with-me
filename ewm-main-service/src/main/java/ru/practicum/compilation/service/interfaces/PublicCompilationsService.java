package ru.practicum.compilation.service.interfaces;

import ru.practicum.compilation.dto.CompilationDto;

import java.util.List;

public interface PublicCompilationsService {

    List<CompilationDto> getAllCompilations(Boolean pinned, int from, int size);

    CompilationDto getCompilationById(Long compId);
}