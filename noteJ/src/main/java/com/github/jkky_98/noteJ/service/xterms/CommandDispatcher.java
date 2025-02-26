package com.github.jkky_98.noteJ.service.xterms;

import com.github.jkky_98.noteJ.domain.mapper.XtermsMapper;
import com.github.jkky_98.noteJ.web.controller.dto.XtermsResponseDto;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class CommandDispatcher {

    private final Map<String, CommandExecutor> commandMap = new HashMap<>();
    private final List<DynamicCommand> dynamicCommands = new ArrayList<>();
    private final XtermsService xtermsService;
    private final XtermsMapper xtermsMapper;

    public CommandDispatcher(XtermsService xtermsService, XtermsMapper xtermsMapper) {
        this.xtermsService = xtermsService;
        this.xtermsMapper = xtermsMapper;

        // 고정 명령어 등록 예시
        commandMap.put("like all", xtermsService::getLikeAll);

        // 동적 명령어 등록: 예를 들어 "like all series = " 뒤에 시리즈 이름이 오는 경우
        Pattern pattern = Pattern.compile("^like all series = \"(.+)\"$", Pattern.CASE_INSENSITIVE);
        Pattern pattern2 = Pattern.compile("^series change \"(.+)\" to \"(.+)\"$", Pattern.CASE_INSENSITIVE);
        dynamicCommands.add(new DynamicCommand(pattern, (cmd, userId, groups) -> {
            // groups.get(1)는 시리즈 이름에 해당합니다.
            String seriesName = groups.get(1);
            return xtermsService.getLikeAllBySeries(cmd, userId, seriesName);
        }));
        dynamicCommands.add(new DynamicCommand(pattern2, (cmd, userId, groups) -> {
            String oldSeriesName = groups.get(1);
            String newSeriesName = groups.get(2);
            return xtermsService.changePostsSeries(userId, oldSeriesName, newSeriesName);
        }));
    }

    public XtermsResponseDto dispatch(String command, Long userId) {
        String result = "";
        if (command == null || command.trim().isEmpty()) {
            result = "명령어가 제공되지 않았습니다.";
        } else {
            String trimmedCommand = command.trim();
            // 먼저 고정 명령어 매핑에서 검색
            CommandExecutor executor = commandMap.get(trimmedCommand.toLowerCase());
            if (executor != null) {
                result = executor.execute(trimmedCommand, userId);
            } else {
                // 동적 명령어 처리
                boolean handled = false;
                for (DynamicCommand dynCmd : dynamicCommands) {
                    System.out.println(dynCmd);
                    var matcher = dynCmd.getPattern().matcher(trimmedCommand);
                    System.out.println(matcher.matches());
                    if (matcher.matches()) {
                        List<String> groups = new ArrayList<>();
                        for (int i = 0; i <= matcher.groupCount(); i++) {
                            groups.add(matcher.group(i));
                        }
                        result = dynCmd.getExecutor().execute(trimmedCommand, userId, groups);
                        handled = true;
                        break;
                    }
                }
                if (!handled) {
                    result = "알 수 없는 명령어: " + command;
                }
            }
        }
        return xtermsMapper.fromResult(result);
    }
}

