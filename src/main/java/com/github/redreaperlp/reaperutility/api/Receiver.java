package com.github.redreaperlp.reaperutility.api;

import com.github.redreaperlp.reaperutility.Main;
import com.github.redreaperlp.reaperutility.enums.EPermission;
import com.github.redreaperlp.reaperutility.events.Configurator;
import com.github.redreaperlp.reaperutility.scheduler.EventScheduler;
import com.github.redreaperlp.reaperutility.storage.JStorage;
import com.github.redreaperlp.reaperutility.storage.server.JGuild;
import com.github.redreaperlp.reaperutility.storage.server.event.JEvent;
import com.github.redreaperlp.reaperutility.util.Color;
import com.google.gson.Gson;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Receiver implements Runnable {
    ServerSocket serverSocket = null;

    @Override
    public void run() {
        ServerSocket serverSocket = null;
        while (true) {
            try {
                serverSocket = new ServerSocket(1234);
                new Color.Print("Receiver", Color.BLUE).append(" started", Color.GREEN).printInfo();
                while (true) {
                    Gson gson = new Gson();
                    Socket socket = serverSocket.accept();
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                    BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    try {
                        String request = reader.readLine();
                        Request req = new Request(request);
                        Answer answer = new Answer();
                        Color.printTest(req.data().toString() + " " + req.request(), Color.ORANGE);
                        Color.printTest(request);
                        switch (req.request()) {
                            case GET_GUILD -> {
                                new Color.Print("Get Guild ", Color.BLUE).append("<" + req.data().toString() + "> ", Color.ORANGE).append("from ", Color.GREEN).append(socket.getInetAddress().getHostAddress(), Color.ORANGE).printDebug();
                                JGuild server = JStorage.instance().getGuild(false, Long.parseLong(req.data().getString("id")));
                                if (server != null) {
                                    answer.set(new JSONObject(gson.toJson(server)));
                                    answer.status(200);
                                } else {
                                    answer.set(new JSONObject().put("reason", "Server not found"));
                                    answer.status(404);
                                }
                            }
                            case GET_EVENTS -> {
                                new Color.Print("Get Events ", Color.BLUE).append("<" + req.data().toString() + "> ", Color.ORANGE).append("from ", Color.GREEN).append(socket.getInetAddress().getHostAddress(), Color.ORANGE).printDebug();
                                JGuild server = JStorage.instance().getGuild(false, Long.parseLong(req.data().getString("id")));
                                Color.printTest(gson.toJson(server.getEvents()));
                                if (server != null) {
                                    JSONArray eventArray = new JSONArray();
                                    for (JEvent event : server.getEvents()) {
                                        JSONObject eventObject = event.getWithInfos();
                                        if (eventObject != null) {
                                            eventArray.put(eventObject);
                                        }
                                    }
                                    answer.set(eventArray);
                                    answer.status(200);
                                } else {
                                    answer.set(new JSONObject().put("reason", "Server not found"));
                                    answer.status(404);
                                }
                            }
                            case GET_GUILDS -> {
                                JSONArray array = new JSONArray();
                                String id = req.data().getString("id");
                                for (JGuild jGuild : JStorage.instance().getGuilds()) {
                                    Guild guild = Main.jda.getGuildById(jGuild.id());
                                    if (guild == null) continue;
                                    Member member = guild.getMemberById(id);
                                    if (member == null) {
                                        try {
                                            member = guild.retrieveMemberById(id).complete();
                                        } catch (Exception e) {
                                            continue;
                                        }
                                    }
                                    if (jGuild.getPermission(EPermission.WEB_PANEL).hasPermission(member))
                                        array.put(jGuild.id());
                                }
                                answer.set(new JSONObject().put("servers", array));
                                answer.status(200);
                            }
                            case MODIFY_EVENT -> {
                                new Color.Print("Modify Event ", Color.BLUE).append("<" + req.data() + ">", Color.ORANGE).printInfo();
                                ERequest.Event_Modify event = gson.fromJson(req.data().toString(), ERequest.Event_Modify.class);
                                JGuild guild = JStorage.instance().getGuild(false, event.guild);
                                if (guild != null) {
                                    JEvent jEvent = guild.getEvent(event.event());
                                    if (jEvent == null) {
                                        answer.set(new JSONObject().put("reason", "Event not found"));
                                        answer.status(404);
                                    } else {
                                        Configurator.FailReason reason = jEvent.setEventDate(event.timestamp());
                                        if (reason == Configurator.FailReason.SUCCESS) {
                                            EventScheduler.schedule(jEvent);
                                            answer.set(new JSONObject(gson.toJson(jEvent)));
                                            answer.status(200);
                                        } else {
                                            answer.set(new JSONObject().put("reason", reason.name()));
                                            answer.status(400);
                                        }
                                    }
                                } else {
                                    answer.set(new JSONObject().put("reason", "Server not found"));
                                    answer.status(400);
                                }
                            }
                            case CHECK_USER_PERMISSION -> {
                                answer.set(new JSONObject().put("reason", "Not implemented"));
                                answer.status(501);
                            }
                            case INVALID -> {
                                new Color.Print("Invalid Request: " + req.data(), Color.RED).printInfo();
                                answer.set(new JSONObject().put("reason", "Invalid Request"));
                                answer.status(400);
                            }
                        }
                        if (answer.status() == 0) {
                            answer.set(new JSONObject().put("reason", "Invalid Request"));
                            answer.status(400);
                        }
                        writer.write(answer.toString());
                        writer.newLine();
                        writer.flush();
                        socket.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                        writer.write(new Answer(500, new JSONObject().put("reason", e.getMessage())).toString());
                        writer.newLine();
                        writer.flush();
                        socket.close();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void close() {
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
