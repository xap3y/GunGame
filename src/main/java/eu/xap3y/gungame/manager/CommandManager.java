package eu.xap3y.gungame.manager;

import eu.xap3y.gungame.GunGame;
import eu.xap3y.gungame.api.enums.LeaderboardType;
import eu.xap3y.gungame.model.Arena;
import org.bukkit.command.CommandSender;
import org.incendo.cloud.SenderMapper;
import org.incendo.cloud.annotations.AnnotationParser;
import org.incendo.cloud.bukkit.BukkitCommandManager;
import org.incendo.cloud.bukkit.CloudBukkitCapabilities;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.paper.LegacyPaperCommandManager;
import org.incendo.cloud.paper.PaperCommandManager;
import org.incendo.cloud.paper.util.sender.PaperSimpleSenderMapper;
import org.incendo.cloud.paper.util.sender.Source;
import org.incendo.cloud.suggestion.Suggestion;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CommandManager {

    private static AnnotationParser<CommandSender> legacyParser;
    private static AnnotationParser<Source> parser;

    public CommandManager() {
        legacyParser = null;
        parser = createParser();
    }

    public CommandManager(boolean legacy) {
        if (legacy) {
            legacyParser = createLegacyParser();
        } else {
            parser = createParser();
        }
    }

    public void parseLegacy(@NotNull Object... instances) {
        legacyParser.parse(instances);
    }

    public void parse(@NotNull Object... instances) {
        parser.parse(instances);
    }

    // LEGACY
    private BukkitCommandManager<CommandSender> createCommandManagerLegacy() {
        ExecutionCoordinator<CommandSender> coordinator = ExecutionCoordinator.asyncCoordinator();
        LegacyPaperCommandManager<CommandSender> manager = new LegacyPaperCommandManager<CommandSender>(
                GunGame.getInstance(),
                coordinator,
                SenderMapper.identity()
        );

        manager.parserRegistry().registerSuggestionProvider("maps", (ctx, input) -> {
            List<Suggestion> suggestions = GunGame.getInstance().getArenaLoader().loadAllArenas().stream()
                    .map(id -> Suggestion.suggestion(id.getArenaName()))
                    .toList();
            return CompletableFuture.completedFuture(suggestions);
        });

        manager.parserRegistry().registerSuggestionProvider("lb", (ctx, input) -> {
            List<Suggestion> suggestions = Arrays.stream(LeaderboardType.values()).toList().stream()
                    .map(id -> Suggestion.suggestion(id.name().toLowerCase()))
                    .toList();
            return CompletableFuture.completedFuture(suggestions);
        });

        manager.parserRegistry().registerSuggestionProvider("maps-disabled", (ctx, input) -> {
            List<Suggestion> suggestions = GunGame.getInstance().getArenaLoader().loadAllArenas().stream()
                    .filter(map -> !map.isEnabled())
                    .map(id -> Suggestion.suggestion(id.getArenaName()))
                    .toList();
            return CompletableFuture.completedFuture(suggestions);
        });

        manager.parserRegistry().registerSuggestionProvider("maps-available", (ctx, input) -> {
            List<Suggestion> suggestions = GunGame.getInstance().getArenaLoader().loadAllArenas().stream()
                    .filter(Arena::isComplete)
                    .map(id -> Suggestion.suggestion(id.getArenaName()))
                    .toList();
            return CompletableFuture.completedFuture(suggestions);
        });

        if (manager.hasCapability(CloudBukkitCapabilities.NATIVE_BRIGADIER)) {
            manager.registerBrigadier();
            manager.brigadierManager().setNativeNumberSuggestions(false);
        }
        return manager;
    }

    // NEW
    private PaperCommandManager<Source> createCommandManager() {
        PaperCommandManager<Source> paperManager = PaperCommandManager
                .builder(PaperSimpleSenderMapper.simpleSenderMapper())
                .executionCoordinator(ExecutionCoordinator.simpleCoordinator())
                .buildOnEnable(GunGame.getInstance());

        paperManager.parserRegistry().registerSuggestionProvider("maps", (ctx, input) -> {
            List<Suggestion> suggestions = GunGame.getInstance().getArenaLoader().getArenaPool().stream()
                    .map(id -> Suggestion.suggestion(id.getArenaName()))
                    .toList();
            return CompletableFuture.completedFuture(suggestions);
        });

        if (paperManager.hasCapability(CloudBukkitCapabilities.NATIVE_BRIGADIER)) {
            paperManager.brigadierManager().setNativeNumberSuggestions(false);
        }
        return paperManager;
    }

    private AnnotationParser<Source> createParser() {
        return new AnnotationParser<>(createCommandManager(), Source.class);
    }

    private AnnotationParser<CommandSender> createLegacyParser() {
        return new AnnotationParser<>(createCommandManagerLegacy(), CommandSender.class);
    }
}

