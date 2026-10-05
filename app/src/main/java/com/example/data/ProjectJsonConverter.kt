package com.example.data

import com.example.model.Clip
import com.example.model.ClipState
import com.example.model.DawProject
import com.example.model.DrumType
import com.example.model.EffectsConfig
import com.example.model.MelodicNote
import com.example.model.Scene
import com.example.model.Track
import com.example.model.TrackType
import org.json.JSONArray
import org.json.JSONObject

object ProjectJsonConverter {

    fun toJson(project: DawProject): String {
        val root = JSONObject()
        root.put("id", project.id)
        root.put("name", project.name)
        root.put("bpm", project.bpm)
        root.put("swing", project.swing.toDouble())
        root.put("masterVolume", project.masterVolume.toDouble())

        // Effects
        val fxObj = JSONObject()
        fxObj.put("filterCutoff", project.effects.filterCutoff.toDouble())
        fxObj.put("filterResonance", project.effects.filterResonance.toDouble())
        fxObj.put("filterType", project.effects.filterType)
        fxObj.put("delayTimeSec", project.effects.delayTimeSec.toDouble())
        fxObj.put("delayFeedback", project.effects.delayFeedback.toDouble())
        fxObj.put("delayMix", project.effects.delayMix.toDouble())
        fxObj.put("reverbSize", project.effects.reverbSize.toDouble())
        fxObj.put("reverbMix", project.effects.reverbMix.toDouble())
        fxObj.put("drive", project.effects.drive.toDouble())
        root.put("effects", fxObj)

        // Scenes
        val scenesArr = JSONArray()
        for (scene in project.scenes) {
            val sc = JSONObject()
            sc.put("index", scene.index)
            sc.put("name", scene.name)
            sc.put("colorHex", scene.colorHex)
            scenesArr.put(sc)
        }
        root.put("scenes", scenesArr)

        // Tracks
        val tracksArr = JSONArray()
        for (track in project.tracks) {
            val tr = JSONObject()
            tr.put("id", track.id)
            tr.put("name", track.name)
            tr.put("type", track.type.name)
            tr.put("colorHex", track.colorHex)
            tr.put("volume", track.volume.toDouble())
            tr.put("pan", track.pan.toDouble())
            tr.put("isMuted", track.isMuted)
            tr.put("isSolo", track.isSolo)
            tr.put("activeClipIndex", track.activeClipIndex ?: -1)

            // Clips
            val clipsArr = JSONArray()
            for (clip in track.clips) {
                val cl = JSONObject()
                cl.put("id", clip.id)
                cl.put("name", clip.name)
                cl.put("sceneIndex", clip.sceneIndex)
                cl.put("trackId", clip.trackId)
                cl.put("colorHex", clip.colorHex)
                cl.put("state", clip.state.name)

                // Drum steps
                val drumObj = JSONObject()
                for ((drumType, steps) in clip.drumSteps) {
                    val sArr = JSONArray()
                    for (step in steps) {
                        sArr.put(step)
                    }
                    drumObj.put(drumType.name, sArr)
                }
                cl.put("drumSteps", drumObj)

                // Melodic notes
                val notesArr = JSONArray()
                for (note in clip.melodicNotes) {
                    val nObj = JSONObject()
                    nObj.put("step", note.step)
                    nObj.put("midiNote", note.midiNote)
                    nObj.put("durationSteps", note.durationSteps)
                    nObj.put("velocity", note.velocity.toDouble())
                    notesArr.put(nObj)
                }
                cl.put("melodicNotes", notesArr)

                clipsArr.put(cl)
            }
            tr.put("clips", clipsArr)

            tracksArr.put(tr)
        }
        root.put("tracks", tracksArr)

        return root.toString()
    }

    fun fromJson(jsonStr: String): DawProject? {
        return try {
            val root = JSONObject(jsonStr)
            val id = root.optLong("id", 0L)
            val name = root.optString("name", "Loaded Project")
            val bpm = root.optInt("bpm", 124)
            val swing = root.optDouble("swing", 0.0).toFloat()
            val masterVolume = root.optDouble("masterVolume", 0.85).toFloat()

            val fxObj = root.optJSONObject("effects")
            val effects = if (fxObj != null) {
                EffectsConfig(
                    filterCutoff = fxObj.optDouble("filterCutoff", 0.85).toFloat(),
                    filterResonance = fxObj.optDouble("filterResonance", 0.2).toFloat(),
                    filterType = fxObj.optInt("filterType", 0),
                    delayTimeSec = fxObj.optDouble("delayTimeSec", 0.25).toFloat(),
                    delayFeedback = fxObj.optDouble("delayFeedback", 0.35).toFloat(),
                    delayMix = fxObj.optDouble("delayMix", 0.2).toFloat(),
                    reverbSize = fxObj.optDouble("reverbSize", 0.5).toFloat(),
                    reverbMix = fxObj.optDouble("reverbMix", 0.25).toFloat(),
                    drive = fxObj.optDouble("drive", 0.15).toFloat()
                )
            } else EffectsConfig()

            val scenes = mutableListOf<Scene>()
            val scenesArr = root.optJSONArray("scenes")
            if (scenesArr != null) {
                for (i in 0 until scenesArr.length()) {
                    val sc = scenesArr.getJSONObject(i)
                    scenes.add(
                        Scene(
                            index = sc.getInt("index"),
                            name = sc.getString("name"),
                            colorHex = sc.getLong("colorHex")
                        )
                    )
                }
            }

            val tracks = mutableListOf<Track>()
            val tracksArr = root.optJSONArray("tracks")
            if (tracksArr != null) {
                for (i in 0 until tracksArr.length()) {
                    val tr = tracksArr.getJSONObject(i)
                    val trackId = tr.getString("id")
                    val trackName = tr.getString("name")
                    val trackType = TrackType.valueOf(tr.getString("type"))
                    val colorHex = tr.getLong("colorHex")
                    val volume = tr.optDouble("volume", 0.8).toFloat()
                    val pan = tr.optDouble("pan", 0.0).toFloat()
                    val isMuted = tr.optBoolean("isMuted", false)
                    val isSolo = tr.optBoolean("isSolo", false)
                    val activeClipIdxRaw = tr.optInt("activeClipIndex", -1)
                    val activeClipIndex = if (activeClipIdxRaw >= 0) activeClipIdxRaw else null

                    val clips = mutableListOf<Clip>()
                    val clipsArr = tr.optJSONArray("clips")
                    if (clipsArr != null) {
                        for (j in 0 until clipsArr.length()) {
                            val cl = clipsArr.getJSONObject(j)
                            val drumMap = mutableMapOf<DrumType, List<Boolean>>()
                            val drumObj = cl.optJSONObject("drumSteps")
                            if (drumObj != null) {
                                val keys = drumObj.keys()
                                while (keys.hasNext()) {
                                    val key = keys.next()
                                    val drumType = DrumType.valueOf(key)
                                    val arr = drumObj.getJSONArray(key)
                                    val list = mutableListOf<Boolean>()
                                    for (k in 0 until arr.length()) {
                                        list.add(arr.getBoolean(k))
                                    }
                                    drumMap[drumType] = list
                                }
                            }

                            val notesList = mutableListOf<MelodicNote>()
                            val notesArr = cl.optJSONArray("melodicNotes")
                            if (notesArr != null) {
                                for (k in 0 until notesArr.length()) {
                                    val n = notesArr.getJSONObject(k)
                                    notesList.add(
                                        MelodicNote(
                                            step = n.getInt("step"),
                                            midiNote = n.getInt("midiNote"),
                                            durationSteps = n.optInt("durationSteps", 1),
                                            velocity = n.optDouble("velocity", 0.8).toFloat()
                                        )
                                    )
                                }
                            }

                            clips.add(
                                Clip(
                                    id = cl.getString("id"),
                                    name = cl.getString("name"),
                                    sceneIndex = cl.getInt("sceneIndex"),
                                    trackId = trackId,
                                    colorHex = cl.getLong("colorHex"),
                                    state = ClipState.valueOf(cl.optString("state", "STOPPED")),
                                    drumSteps = drumMap,
                                    melodicNotes = notesList
                                )
                            )
                        }
                    }

                    tracks.add(
                        Track(
                            id = trackId,
                            name = trackName,
                            type = trackType,
                            colorHex = colorHex,
                            volume = volume,
                            pan = pan,
                            isMuted = isMuted,
                            isSolo = isSolo,
                            clips = clips,
                            activeClipIndex = activeClipIndex
                        )
                    )
                }
            }

            DawProject(
                id = id,
                name = name,
                bpm = bpm,
                swing = swing,
                masterVolume = masterVolume,
                tracks = tracks,
                scenes = scenes,
                effects = effects
            )
        } catch (_: Exception) {
            null
        }
    }
}
