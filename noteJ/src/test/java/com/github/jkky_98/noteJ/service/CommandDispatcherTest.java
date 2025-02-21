package com.github.jkky_98.noteJ.service;

import com.github.jkky_98.noteJ.domain.mapper.XtermsMapper;
import com.github.jkky_98.noteJ.service.xterms.CommandDispatcher;
import com.github.jkky_98.noteJ.service.xterms.XtermsService;
import com.github.jkky_98.noteJ.web.controller.dto.XtermsResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("[CommandDispatcher] Unit Tests")
public class CommandDispatcherTest {
    @Mock
    private XtermsService xtermsService;

    @Mock
    private XtermsMapper xtermsMapper;

    @InjectMocks
    private CommandDispatcher commandDispatcher;

    // 테스트용 XtermsResponseDto. 실제 DTO에 맞게 생성자나 필드를 조정하세요.
    private XtermsResponseDto fixedResponse;
    private XtermsResponseDto dynamicResponse;
    private XtermsResponseDto unknownResponse;
    private XtermsResponseDto emptyResponse;

    @BeforeEach
    void setUp() {
        // 테스트용 DTO 생성 (여기서는 단순 문자열을 담는 DTO로 가정)
        fixedResponse = new XtermsResponseDto("test result fixed");
        dynamicResponse = new XtermsResponseDto("test result dynamic");
        unknownResponse = new XtermsResponseDto("알 수 없는 명령어: unknown command");
        emptyResponse = new XtermsResponseDto("명령어가 제공되지 않았습니다.");
    }

    @Test
    @DisplayName("dispatch() - null 또는 빈 명령어")
    void testDispatch_nullOrEmptyCommand() {
        // when
        when(xtermsMapper.fromResult("명령어가 제공되지 않았습니다.")).thenReturn(emptyResponse);
        XtermsResponseDto resultNull = commandDispatcher.dispatch(null, 1L);
        XtermsResponseDto resultEmpty = commandDispatcher.dispatch("   ", 1L);


        // then
        verify(xtermsMapper, times(2)).fromResult("명령어가 제공되지 않았습니다.");
        // 실제 DTO 비교 (equals() 구현에 따라 조정)
        assertThat(resultNull).isEqualTo(emptyResponse);
        assertThat(resultEmpty).isEqualTo(emptyResponse);
    }

    @Test
    @DisplayName("dispatch() - 고정 명령어 'like all'")
    void testDispatch_fixedCommand() {
        String command = "like all";
        // 고정 명령어는 commandMap에 등록되어 있으므로, xtermsService.getLikeAll()가 호출됨
        when(xtermsService.getLikeAll(command, 1L)).thenReturn("test result fixed");
        when(xtermsMapper.fromResult("test result fixed")).thenReturn(fixedResponse);

        XtermsResponseDto result = commandDispatcher.dispatch(command, 1L);

        assertThat(result).isEqualTo(fixedResponse);
        verify(xtermsService).getLikeAll(command, 1L);
        verify(xtermsMapper).fromResult("test result fixed");
    }

    @Test
    @DisplayName("dispatch() - 동적 명령어 'like all series = \"SomeSeries\"'")
    void testDispatch_dynamicCommand() {
        String command = "like all series = \"SomeSeries\"";
        // 동적 명령어의 경우, 패턴에 매칭되어 seriesName을 추출하고 xtermsService.getLikeAllBySeries()가 호출됨.
        when(xtermsService.getLikeAllBySeries(command, 1L, "SomeSeries")).thenReturn("test result dynamic");
        when(xtermsMapper.fromResult("test result dynamic")).thenReturn(dynamicResponse);

        XtermsResponseDto result = commandDispatcher.dispatch(command, 1L);

        assertThat(result).isEqualTo(dynamicResponse);
        verify(xtermsService).getLikeAllBySeries(command, 1L, "SomeSeries");
        verify(xtermsMapper).fromResult("test result dynamic");
    }

    @Test
    @DisplayName("dispatch() - 알 수 없는 명령어")
    void testDispatch_unknownCommand() {
        String command = "unknown command";
        String expectedResult = "알 수 없는 명령어: " + command;
        when(xtermsMapper.fromResult(expectedResult)).thenReturn(unknownResponse);

        XtermsResponseDto result = commandDispatcher.dispatch(command, 1L);

        assertThat(result).isEqualTo(unknownResponse);
        verify(xtermsMapper).fromResult(expectedResult);
    }
}
