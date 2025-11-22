(define (domain lunar-extended)

;;; DOMAIN DEFINITION FOR LUNAR EXPLORATION MISSION
;;; This domain models the autonomous operations of a rover on the lunar surface
;;; including navigation, data collection, and sample return.

(:requirements :strips :typing :negative-preconditions :universal-preconditions)

;;; TYPES
;;; Basic types of objects in the domain
(:types
    ;;;The following defines the object types involved in the task.
    location   ;Represents positions on the lunar surface
    rover      ;Mobile robot that performs exploration tasks
    lander     ;Stationary base that deploys the rover and stores samples
    data       ;Information collected by the rover (images or scans)
    sample     ;Physical samples collected from the lunar surface
    - object
    
    image_data ;High-resolution images of geological features
    scan_data  ;Subsurface scanning data from ground-penetrating radar
    - data   

    astronaut   ; Human crew members assisting mission operations
    area        ; Internal compartments within lander structures
    - object

    control_room 
    docking_bay 
    - area  ; Specific lander internal areas
)

;;; PREDICATES
;;; State descriptors for the lunar mission domain
(:predicates
    ;; Location and connectivity
    (at ?r - rover ?l - location)                ; Rover is at specific location
    (lander_at ?l - lander ?loc - location)      ; Lander is positioned at location
    (connected ?from ?to - location)             ; Path exists between two locations
    
    ;; Rover status and capabilities
    (deployed ?r - rover)                        ; Rover has been deployed from lander
    (lander_associated ?r - rover ?l - lander)   ; Rover belongs to specific lander
    
    ;; Data management
    (has_data ?r - rover ?d - data)              ; Rover is currently storing data
    (data_transmitted ?d - data)                 ; data has been transmitted to lander
    
    ;; Mission task assignments
    (image_required_at ?img - image_data ?loc - location)    ; Position of image collection task 
    (scan_required_at ?scan - scan_data ?loc - location)     ; Position of scan collection task assignment  
    (sample_required_at ?s - sample ?loc - location)         ; Position of sample collection task assignment
    
    ;; Task completion tracking
    (image_task_completed ?img - image_data)                 ; Image task completion status
    (scan_task_completed ?scan - scan_data)                  ; Scan task completion status
    (sample_task_completed ?s - sample)                      ; Sample task completion status
    
    ;; Sample management
    (has_sample ?r - rover ?s - sample)          ; Rover is carrying a sample
    (sample_collected ?s - sample)               ; Sample has been picked up
    (sample_stored ?s - sample ?l - lander)      ; Sample is stored in lander
    
    ;; Resource availability flags
    (rover_data_available ?r - rover)            ; Rover can perform new data collection
    (rover_sample_available ?r - rover)          ; Rover can perform new sample collection
    (lander_storage_free ?l - lander)            ; Lander has not stored the sample
    
    ;; Mission completion status
    (all_tasks_completed)                        ; All mission tasks are finished

     ;; === NEW: Astronaut management predicates ===
    (astronaut_in_docking_bay ?l - lander)                   ; Docking bay occupancy status
    (astronaut_in_control_room ?l - lander)                  ; Control room occupancy status
    (astronaut_stationed ?a - astronaut ?l - lander)  ; Astronauts are stationed at a specific lander
)

;;; ACTIONS

(:action deploy_rover
    ;;; Deploys the rover from its lander to begin mission operations
    :parameters (?r - rover ?l - lander ?loc - location)
    :precondition (and
        (lander_at ?l ?loc)                 ; Lander must be at location
        (lander_associated ?r ?l)           ; Rover must belong to this lander
        (not (deployed ?r))                 ; Rover must not already be deployed
        (astronaut_in_docking_bay ?l)       ; Astronaut must be in docking bay for deployment
    )
    :effect (and
        (deployed ?r)                       ; Mark rover as deployed
        (at ?r ?loc)                        ; Position rover at lander location
        (rover_data_available ?r)           ; Rover is ready for data collection
        (rover_sample_available ?r)         ; Rover is ready for sample collection
        (lander_storage_free ?l)   ; Lander has not stored the sample
    )
)

(:action move
    ;;; Moves the rover between connected locations on the lunar surface
    :parameters (?r - rover ?from ?to - location)
    :precondition (and
        (at ?r ?from)                       ; Rover must be at starting location
        (connected ?from ?to)               ; Path must exist between locations
        (deployed ?r)                       ; Rover must be deployed
    )
    :effect (and
        (not (at ?r ?from))                 ; No longer at starting location
        (at ?r ?to)                         ; Now at destination location
    )
)

(:action take_image
    ;;; Captures high-resolution images of geological features at specific locations
    :parameters (?r - rover ?l - location ?img - image_data)
    :precondition (and
        (at ?r ?l)                          ; Rover must be at target location
        (deployed ?r)                       ; Rover must be deployed
        (rover_data_available ?r)           ; Rover memory must be available
        (image_required_at ?img ?l)         ; Image collection required at this location
        (not (image_task_completed ?img))   ; Task not yet completed
    )
    :effect (and
        (has_data ?r ?img)                  ; Rover now stores the image data
        (image_task_completed ?img)         ; Mark task as completed
        (not (rover_data_available ?r))     ; Rover memory now occupied
    )
)

(:action perform_scan
    ;;; Executes subsurface scanning using ground-penetrating radar
    :parameters (?r - rover ?l - location ?scan - scan_data)
    :precondition (and
        (at ?r ?l)                          ; Rover must be at target location
        (deployed ?r)                       ; Rover must be deployed
        (rover_data_available ?r)           ; Rover memory must be available
        (scan_required_at ?scan ?l)         ; Scan collection required at this location
        (not (scan_task_completed ?scan))   ; Task not yet completed
    )
    :effect (and
        (has_data ?r ?scan)                 ; Rover now stores the scan data
        (not (rover_data_available ?r))     ; Rover memory now occupied
        (scan_task_completed ?scan)         ; Mark task as completed
    )
)

(:action transmit_data
    ;;; Transmits collected data wirelessly back to the associated lander
    :parameters (?r - rover ?l - lander ?d - data)
    :precondition (and
        (lander_associated ?r ?l)           ; Rover must belong to this lander
        (has_data ?r ?d)                    ; Rover must have the data
        (not (data_transmitted ?d))         ; Data must not already be transmitted
        (astronaut_in_control_room ?l)      ; Astronaut must be in control room for transmission
    )
    :effect (and
        (data_transmitted ?d)               ; Mark data as transmitted
        (not (has_data ?r ?d))              ; Clear rover's memory
        (rover_data_available ?r)           ; Rover can now collect new data
    )
)

(:action collect_sample
    ;;; Collects a physical sample from the current location
    :parameters (?r - rover ?l - location ?s - sample)
    :precondition (and
        (at ?r ?l)                          ; Rover must be at sample location
        (deployed ?r)                       ; Rover must be deployed
        (rover_sample_available ?r)         ; Rover sample capacity must be available
        (not (has_sample ?r ?s))            ; Rover now does not carry the sample
        (sample_required_at ?s ?l)          ; Sample collection required at this location
        (not (sample_collected ?s))         ; Sample must not already be collected
        (not (sample_task_completed ?s))    ; Task not yet completed
    )
    :effect (and
        (has_sample ?r ?s)                  ; Rover now carries the sample
        (sample_collected ?s)               ; Mark sample as collected
        (not (rover_sample_available ?r))   ; Rover is now occupied with sample
        (sample_task_completed ?s)          ; Mark task as completed
    )
)

(:action check_mission_completion
    ;;; Checks if all mission tasks have been completed
    :parameters ()
    :precondition (and
        ;; All the images of the assigned tasks have been transmitted.
        (forall (?img - image_data)
            (and (image_task_completed ?img) (data_transmitted ?img)))
        
        ;; All the scans of the assigned tasks have been transmitted.
        (forall (?scan - scan_data)
            (and (scan_task_completed ?scan) (data_transmitted ?scan)))
        
        ;; All the samples of the assigned tasks have been collected.
        (forall (?s - sample)
            (sample_task_completed ?s))
        
        (not (all_tasks_completed))
    )
    :effect (and
        (all_tasks_completed)
    )
)
(:action store_sample
    ;;; Stores a collected sample in the lander (subject to storage constraints)
    :parameters (?r - rover ?l - lander ?s - sample ?loc - location)
    :precondition (and
        (at ?r ?loc)                        ; Rover must be at lander location
        (lander_at ?l ?loc)                 ; Lander must be at same location
        (lander_associated ?r ?l)           ; Rover must belong to this lander
        (has_sample ?r ?s)                  ; Rover must be carrying the sample
        (lander_storage_free ?l)            ; Lander must not already have the sample
        (all_tasks_completed)               ; All data and sample must be collected
        (astronaut_in_docking_bay ?l)       ; Astronaut must be in docking bay for storage 
    )
    :effect (and
        (sample_stored ?s ?l)               ; Sample is stored in lander
        (not (lander_storage_free ?l))      ; Lander now contains the sample
        (not (has_sample ?r ?s))            ; Rover no longer carries sample
    )
)

;;; Astronaut movement action (within specified features)

;;; Astronaut movement action (within specified features) 
(:action move_astronaut_to_control_room
    ;;; Transfers astronaut from docking bay to control room within the same lander
    :parameters (?a - astronaut ?l - lander)
    :precondition (and
        (astronaut_stationed ?a ?l)        ; Astronaut must be stationed in this lander
        (astronaut_in_docking_bay ?l)      ; Astronaut must be present in lander's docking bay
    )
    :effect (and
        (astronaut_stationed ?a ?l) 
        (not (astronaut_in_docking_bay ?l)) ; Clear astronaut presence from docking bay
        (astronaut_in_control_room ?l)      ; Establish astronaut presence in control room
    )
)

(:action move_astronaut_to_docking_bay
    ;;; Transfers astronaut from control room to docking bay within the same lander
    :parameters (?a - astronaut ?l - lander)
    :precondition (and
        (astronaut_stationed ?a ?l)        ; Astronaut must be stationed in this lander
        (astronaut_in_control_room ?l)     ; Astronaut must be present in lander's control room
    )
    :effect (and
        (not (astronaut_in_control_room ?l)) ; Clear astronaut presence from control room
        (astronaut_in_docking_bay ?l)        ; Establish astronaut presence in docking bay
    )
)
)