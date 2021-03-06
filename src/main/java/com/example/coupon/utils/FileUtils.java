package com.example.coupon.utils;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.nio.file.Files;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.BaseStream;
import java.util.stream.Collectors;

@Slf4j
@UtilityClass
public class FileUtils {
    public final String CSV_SEPARATOR = ",";
    public final int CSV_HEADER_INDEX = 0;
    public final String TEMP_S = "temp/%s";

    public Mono<Void> readFilePartFlux(Flux<FilePart> filePartFlux, int batchSize, BiFunction<List<String>, List<String>, Mono<?>> insertCouponNumbers) {
        return filePartFlux.flatMap(filePart -> {
            String tempFilePath = String.format(TEMP_S, filePart.filename());
            File file = new File(tempFilePath);

            return Mono.zip(
                    FileUtils.copyTempFile(filePart, file),
                    Mono.delay(Duration.ofSeconds(1))
                            .then(FileUtils.readCsvFile(file, batchSize, insertCouponNumbers))
                            .flatMap(FileUtils::deleteTempFile)
            );
        }).then();
    }

    public Mono<File> copyTempFile(FilePart filePart, File file) {
        return filePart.transferTo(file)
                .then(Mono.just(file));
    }

    public Mono<File> readCsvFile(File file, int batchSize, BiFunction<List<String>, List<String>, Mono<?>> consumer) {
        List<String> fields = new ArrayList<>();
        return Flux.using(() -> Files.lines(file.toPath()), Flux::fromStream, BaseStream::close)
                .buffer(batchSize)
                .flatMap(strings -> {
                    int skip = 0;
                    if (fields.isEmpty()) {
                        fields.addAll(Arrays.asList(strings.get(CSV_HEADER_INDEX).split(CSV_SEPARATOR)));
                        skip++;
                    }
                    return consumer.apply(fields, skipList(strings, skip));
                }).then(Mono.just(file));
    }

    private List<String> skipList(List<String> strings, int skip) {
        return strings.stream()
                .skip(skip)
                .filter(it -> !Strings.isBlank(it))
                .collect(Collectors.toList());
    }

    public Mono<Void> deleteTempFile(File file) {
        boolean isDeleted = file.delete();
        log.info("임시 파일 삭제 여부 : filePath >>> {}, flag >>> {}", file.getName(), isDeleted);
        return Mono.empty();
    }
}
