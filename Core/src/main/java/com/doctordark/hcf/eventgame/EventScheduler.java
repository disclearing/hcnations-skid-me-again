package com.doctordark.hcf.eventgame;

import com.doctordark.hcf.HCF;
import com.doctordark.hcf.eventgame.faction.KothFaction;
import org.bukkit.Bukkit;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Class that can handle schedules for game events.
 */
public class EventScheduler implements IEventScheduler{

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final String FILE_NAME = "eventSchedules.txt";
    private static final long QUERY_DELAY = TimeUnit.HOURS.toMillis(1);
    private final Map<LocalDateTime, String> scheduleMap = new LinkedHashMap<>();
    private final HCF plugin;
    private boolean isReloading = false;
    private long lastQuery;

    public EventScheduler(HCF plugin){
        this.plugin = plugin;
        reloadSchedules();
    }

    private void reloadSchedules(){
        scheduleMap.clear();
        isReloading = true;

        if(plugin.getConfiguration().isKitMap()){
            List<KothFaction> koths = plugin.getFactions().getFactionManager().getFactions().stream().filter(faction ->
                    faction instanceof KothFaction).map(faction -> (KothFaction) faction).collect(Collectors.toList());

            if(koths.isEmpty()){
                return;
            }

            LocalDateTime now = LocalDateTime.now(plugin.getConfiguration().getServerTimeZoneID());

            int assigned = 0;
            String lastPicked = null;
            while(scheduleMap.size() < 8){
                KothFaction koth = koths.get(ThreadLocalRandom.current().nextInt(koths.size()));

                if(koth.getCaptureZone() == null){
                    continue;
                }

                if(lastPicked == null || !(koths.size() > 1 && koth.getName().equals(lastPicked))){
                    assigned++;
                    lastPicked = koth.getName();

                    int assignedHour;
                    if(assigned == 1){
                        assignedHour = 3;
                    }else if(assigned == 2){
                        assignedHour = 6;
                    }else if(assigned == 3){
                        assignedHour = 9;
                    }else if(assigned == 4){
                        assignedHour = 11;
                    }else if(assigned == 5){
                        assignedHour = 15;
                    }else if(assigned == 6){
                        assignedHour = 17;
                    }else if(assigned == 7){
                        assignedHour = 21;
                    }else if(assigned == 8){
                        assignedHour = 23;
                    }else{
                        // should be impossible
                        continue;
                    }

                    int assignedDay = now.getDayOfMonth();
                    if(now.getHour() > assignedHour){
                        assignedDay++;
                    }

                    if(assignedDay > now.getMonth().maxLength()){
                        assignedDay = 1;
                    }

                    LocalDateTime time;
                    try{
                        time = LocalDateTime.of(now.getYear(), now.getMonth(), assignedDay, assignedHour, 0);
                    }catch(DateTimeException e){
                        int monthFix = now.getMonth().getValue() + 1;
                        time = LocalDateTime.of(now.getYear(), monthFix > 12 ? 1 : monthFix, assignedDay, assignedHour, 0);
                    }

                    scheduleMap.put(time, koth.getName());
                    plugin.getLogger().info("Assigning " + koth.getName() + " for " + time.toString());
                }
            }

        }else{
            try(BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(plugin.getDataFolder(), FILE_NAME)), StandardCharsets.UTF_8))){
                String currentLine;
                while((currentLine = bufferedReader.readLine()) != null){
                    if(currentLine.startsWith("#")){
                        continue;
                    }

                    String[] args = currentLine.split("=");
                    if(args.length == 2){
                        LocalDateTime localDateTime;

                        try{
                            localDateTime = LocalDateTime.parse(args[0], DATE_TIME_FORMATTER);
                        }catch(DateTimeParseException ex){
                            ex.printStackTrace();
                            continue;
                        }

                        if(scheduleMap.containsKey(localDateTime)){
                            continue; //Don't add what already exists
                        }

                        plugin.getLogger().info("Setting KOTH time for " + args[1] + " to " + localDateTime.toLocalDate() + " " + localDateTime.toLocalTime());

                        this.scheduleMap.put(localDateTime, args[1]);
                        continue;
                    }

                    plugin.getLogger().warning("Skipping KOTH line: " + currentLine);
                }
            }catch(FileNotFoundException ex){
                Bukkit.getConsoleSender().sendMessage("Could not find file " + FILE_NAME + '.');
            }catch(IOException ex){
                ex.printStackTrace();
            }

        }

        lastQuery = System.currentTimeMillis();
        isReloading = false;
    }

    @Override
    public Map<LocalDateTime, String> getScheduleMap(){
        long millis = System.currentTimeMillis();
        if(millis - QUERY_DELAY > lastQuery && !isReloading){
            this.reloadSchedules();
            this.lastQuery = millis;
        }

        return scheduleMap;
    }
}
