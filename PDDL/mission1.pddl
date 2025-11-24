(define (problem lunar-mission-1)
    (:domain lunar)
    
    (:objects
        ;; Lunar surface navigation waypoints
        wp1 wp2 wp3 wp4 wp5 - location
        
        ;; Mission hardware 
        rover1 - rover
        lander1 - lander
        
        ;; Scientific data collection targets
        image1 - image_data
        scan1 - scan_data
        
        ;; Geological sample retrieval target
        sample1 - sample
    )
    
    ;; INITIAL MISSION STATE CONFIGURATION
    (:init
        ;;LANDER DEPLOYMENT CONFIGURATION
        (lander_at lander1 wp1)              ; Lander touchdown at waypoint 1
        (lander_associated rover1 lander1)   ; Rover assignment to lander
        
        ;;ROVER PRE-DEPLOYMENT STATE
        (not (deployed rover1))              ; Rover stowed in lander
        (rover_data_available rover1)        ; Data storage initialized
        (rover_sample_available rover1)      ; Sample capacity available
        (lander_storage_free lander1)        ; Lander storage available for sample
        
        ;;LUNAR SURFACE TRAVERSAL NETWORK 
        (connected wp1 wp2)                  ; Waypoint 1 to Waypoint 2
        (connected wp1 wp4)                  ; Waypoint 1 to Waypoint 4
        (connected wp2 wp3)                  ; Waypoint 2 to Waypoint 3
        (connected wp3 wp5)                  ; Waypoint 3 to Waypoint 5
        (connected wp5 wp1)                  ; Waypoint 5 to Waypoint 1
        (connected wp4 wp3)                  ; Waypoint 4 to Waypoint 3
        
        ;;MISSION TASK ASSIGNMENTS
        (image_required_at image1 wp5)       ; Capture image at waypoint 5
        (scan_required_at scan1 wp3)         ; Perform scan at waypoint 3
        (sample_required_at sample1 wp1)     ; Collect sample at waypoint 1
        
        ;;SCIENTIFIC DATA INITIAL STATE
        (not (image_task_completed image1))  ; Image capture not yet performed
        (not (scan_task_completed scan1))    ; Scan operation not yet performed
        (not (data_transmitted image1))      ; Image data not transmitted
        (not (data_transmitted scan1))       ; Scan data not transmitted
        
        ;;SAMPLE INITIAL STATE
        (not (sample_collected sample1))     ; Sample not yet collected
        (not (sample_task_completed sample1)); Sample task not completed
        (not (sample_stored sample1 lander1)); Sample not stored in lander
        (not (has_sample rover1 sample1))    ; Rover not carrying sample
        
        ;;ROVER STORAGE INITIAL STATE =====
        (not (has_data rover1 image1))       ; Rover not storing image data
        (not (has_data rover1 scan1))        ; Rover not storing scan data
        
        ;; ===== MISSION COMPLETION STATUS =====
        (not (all_tasks_completed))          ; Mission tasks incomplete
    )
    
    ;; MISSION SUCCESS CRITERIA
    (:goal (and
        ;; Primary scientific objectives
        (data_transmitted image1)           ; High-resolution image from WP5 transmitted
        (data_transmitted scan1)            ; Subsurface scan from WP3 transmitted
        (sample_stored sample1 lander1)     ; Geological sample from WP1 stored in lander
    ))
)
