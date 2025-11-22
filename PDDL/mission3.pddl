(define (problem lunar-mission-3)
    (:domain lunar-extended)
    
    ;; MISSION ENTITY DECLARATIONS
    (:objects
        ;; Lunar surface navigation waypoints (6 locations)
        wp1 wp2 wp3 wp4 wp5 wp6 - location
        
        ;; Mission hardware assets (2 rovers and 2 landers)
        rover1 rover2 - rover
        lander1 lander2 - lander
        
        ;; Scientific data collection targets
        image1 image2 - image_data
        scan1 scan2 - scan_data
        sample1 sample2 - sample
        
        ;; Astronaut personnel
        alice bob - astronaut
        
        ;; Lander internal areas (for type completeness)
        control_room1 docking_bay1 - area  ; Lander1 internal compartments
        control_room2 docking_bay2 - area  ; Lander2 internal compartments
    )
    
    ;; INITIAL MISSION STATE CONFIGURATION
    (:init
        ;; ANDER AND ROVER CONFIGURATION
        ;; Lander deployment positions
        (lander_at lander1 wp2)              ; Lander1 positioned at waypoint 2
        (lander_at lander2 wp1)              ; Lander2 positioned at waypoint 1
        
        ;; Rover-lander associations
        (lander_associated rover1 lander1)   ; Rover1 assigned to Lander1
        (lander_associated rover2 lander2)   ; Rover2 assigned to Lander2
        
        ;; Rover1 initial state (pre-deployed at WP2)
        (deployed rover1)                    ; Rover1 already deployed from lander
        (at rover1 wp2)                      ; Rover1 positioned at waypoint 2
        (rover_data_available rover1)        ; Rover1 data storage initialized
        (rover_sample_available rover1)      ; Rover1 sample capacity available
        
        ;; Rover2 initial state (undeployed)
        (not (deployed rover2))              ; Rover2 stored inside Lander2
        (rover_data_available rover2)        ; Rover2 data storage initialized
        (rover_sample_available rover2)      ; Rover2 sample capacity available
        
        ;; Lander storage status
        (lander_storage_free lander1)        ; Lander1 sample storage available
        (lander_storage_free lander2)        ; Lander2 sample storage available
        
        ;; LUNAR SURFACE NAVIGATION NETWORK
        (connected wp1 wp2)                  ; Waypoint 1 ↔ Waypoint 2
        (connected wp2 wp1)
        (connected wp2 wp4)                  ; Waypoint 2 ↔ Waypoint 4
        (connected wp4 wp2)
        (connected wp2 wp3)                  ; Waypoint 2 → Waypoint 3
        (connected wp3 wp5)                  ; Waypoint 3 → Waypoint 5
        (connected wp5 wp3)                  ; Waypoint 5 → Waypoint 3
        (connected wp5 wp6)                  ; Waypoint 5 → Waypoint 6
        (connected wp6 wp4)                  ; Waypoint 6 → Waypoint 4
        
        ;; MISSION TASK ASSIGNMENTS
        ;; Image capture task specifications
        (image_required_at image1 wp3)       ; Capture image1 at waypoint 3
        (image_required_at image2 wp2)       ; Capture image2 at waypoint 2
        
        ;; Subsurface scan task specifications
        (scan_required_at scan1 wp4)         ; Perform scan1 at waypoint 4
        (scan_required_at scan2 wp6)         ; Perform scan2 at waypoint 6
        
        ;; Sample collection task specifications
        (sample_required_at sample1 wp5)     ; Collect sample1 at waypoint 5
        (sample_required_at sample2 wp1)     ; Collect sample2 at waypoint 1
        
        ;; SCIENTIFIC DATA INITIAL STATE
        ;; Image task completion status
        (not (image_task_completed image1))  ; Image1 task not completed
        (not (image_task_completed image2))  ; Image2 task not completed
        
        ;; Scan task completion status
        (not (scan_task_completed scan1))    ; Scan1 task not completed
        (not (scan_task_completed scan2))    ; Scan2 task not completed
        
        ;; Data transmission status
        (not (data_transmitted image1))      ; Image1 data not transmitted
        (not (data_transmitted image2))      ; Image2 data not transmitted
        (not (data_transmitted scan1))       ; Scan1 data not transmitted
        (not (data_transmitted scan2))       ; Scan2 data not transmitted
        
        ;; SAMPLE MANAGEMENT INITIAL STATE
        ;; Sample collection status
        (not (sample_collected sample1))     ; Sample1 not collected
        (not (sample_collected sample2))     ; Sample2 not collected
        
        ;; Sample task completion status
        (not (sample_task_completed sample1)); Sample1 task not completed
        (not (sample_task_completed sample2)); Sample2 task not completed
        
        ;; Sample storage status
        (not (sample_stored sample1 lander1)); Sample1 not stored in lander1
        (not (sample_stored sample1 lander2)); Sample1 not stored in lander2
        (not (sample_stored sample2 lander1)); Sample2 not stored in lander1
        (not (sample_stored sample2 lander2)); Sample2 not stored in lander2
        
        ;; Rover sample carrying status
        (not (has_sample rover1 sample1))    ; Rover1 not carrying sample1
        (not (has_sample rover1 sample2))    ; Rover1 not carrying sample2
        (not (has_sample rover2 sample1))    ; Rover2 not carrying sample1
        (not (has_sample rover2 sample2))    ; Rover2 not carrying sample2
        
        ;; ROVER DATA STORAGE INITIAL STATE
        (not (has_data rover1 image1))       ; Rover1 not storing image1
        (not (has_data rover1 image2))       ; Rover1 not storing image2
        (not (has_data rover1 scan1))        ; Rover1 not storing scan1
        (not (has_data rover1 scan2))        ; Rover1 not storing scan2
        (not (has_data rover2 image1))       ; Rover2 not storing image1
        (not (has_data rover2 image2))       ; Rover2 not storing image2
        (not (has_data rover2 scan1))        ; Rover2 not storing scan1
        (not (has_data rover2 scan2))        ; Rover2 not storing scan2
        
        ;; MISSION COMPLETION STATUS
        (not (all_tasks_completed))          ; Mission tasks incomplete
               
        ;;Astronaut station assignments
        (astronaut_stationed alice lander1)  ; Alice permanently assigned to Lander 1
        (astronaut_stationed bob lander2)    ; Bob permanently assigned to Lander 2

        ;; Initial astronaut positions (as specified in mission requirements)
        (astronaut_in_docking_bay lander1)   ; Alice initially in Lander1 docking bay
        (astronaut_in_control_room lander2)  ; Bob initially in Lander2 control room
        
        ;; Complementary area status (other areas initially unoccupied)
        (not (astronaut_in_control_room lander1)) ; Lander1 control room initially empty
        (not (astronaut_in_docking_bay lander2))  ; Lander2 docking bay initially empty
    )
    
    ;; MISSION SUCCESS CRITERIA
    (:goal (and
        ;; Scientific data transmission objectives
        (data_transmitted image1)           ; High-resolution image from WP3 transmitted
        (data_transmitted image2)           ; High-resolution image from WP2 transmitted
        (data_transmitted scan1)            ; Subsurface scan from WP4 transmitted
        (data_transmitted scan2)            ; Subsurface scan from WP6 transmitted
        
        ;; Geological sample storage objectives
        (sample_stored sample1 lander1)     ; Sample from WP5 stored in Lander1
        (sample_stored sample2 lander2)     ; Sample from WP1 stored in Lander2
    ))
)