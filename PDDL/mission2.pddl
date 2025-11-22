(define (problem lunar-mission-2)
    (:domain lunar)
    
    ;; MISSION ENTITY DECLARATIONS
    (:objects
        ;; Lunar surface navigation waypoints (6 locations)
        wp1 wp2 wp3 wp4 wp5 wp6 - location
        
        ;; Mission hardware assets (2 rovers and 2 landers)
        rover1 rover2 - rover
        lander1 lander2 - lander
        
        ;; Scientific data collection targets
        image1 image2 - image_data      ; Two image collection tasks
        scan1 scan2 - scan_data         ; Two subsurface scan tasks
        
        ;; Geological sample retrieval targets  
        sample1 sample2 - sample        ; Two physical sample collection tasks
    )
    
    ;; INITIAL MISSION STATE CONFIGURATION
    (:init
        ;; LANDER DEPLOYMENT CONFIGURATION
        (lander_at lander1 wp2)              ; Lander1 touchdown at waypoint 2
        (lander_at lander2 wp1)              ; Lander2 touchdown at waypoint 1
        
        ;; ROVER-LANDER ASSOCIATIONS
        (lander_associated rover1 lander1)   ; Rover1 assigned to Lander1
        (lander_associated rover2 lander2)   ; Rover2 assigned to Lander2
        
        ;; ROVER1 INITIAL STATE (PRE-DEPLOYED AT WP2)
        (deployed rover1)                    ; Rover1 already deployed
        (at rover1 wp2)                      ; Rover1 positioned at waypoint 2
        (rover_data_available rover1)        ; Rover1 data storage available
        (rover_sample_available rover1)      ; Rover1 sample capacity available
        
        ;; ROVER2 INITIAL STATE (UNDEPLOYED)
        (not (deployed rover2))              ; Rover2 stowed in Lander2
        (rover_data_available rover2)        ; Rover2 data storage initialized
        (rover_sample_available rover2)      ; Rover2 sample capacity available
        
        ;; LANDER STORAGE STATUS
        (lander_storage_free lander1)        ; Lander1 storage available
        (lander_storage_free lander2)        ; Lander2 storage available
        
        ;; LUNAR SURFACE TRAVERSAL NETWORK
        (connected wp1 wp2)                  ; Waypoint 1 ↔ Waypoint 2
        (connected wp2 wp1)                  
        (connected wp2 wp4)                  ; Waypoint 2 ↔ Waypoint 4  
        (connected wp4 wp2)
        (connected wp2 wp3)                  ; Waypoint 2 → Waypoint 3
        (connected wp3 wp5)                  ; Waypoint 3 → Waypoint 5
        (connected wp5 wp3)                  ; Waypoint 5 ↔ Waypoint 3
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
        (not (image_task_completed image1))  ; Image1 capture not yet performed
        (not (image_task_completed image2))  ; Image2 capture not yet performed
        
        ;; Scan task completion status
        (not (scan_task_completed scan1))    ; Scan1 operation not yet performed  
        (not (scan_task_completed scan2))    ; Scan2 operation not yet performed
        
        ;; Data transmission status
        (not (data_transmitted image1))      ; Image1 data not transmitted
        (not (data_transmitted image2))      ; Image2 data not transmitted
        (not (data_transmitted scan1))       ; Scan1 data not transmitted
        (not (data_transmitted scan2))       ; Scan2 data not transmitted
        
        ;; SAMPLE MANAGEMENT INITIAL STATE
        ;; Sample collection status
        (not (sample_collected sample1))     ; Sample1 not yet collected
        (not (sample_collected sample2))     ; Sample2 not yet collected
        
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