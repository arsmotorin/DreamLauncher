// Copyright 2025 © Arsenii Motorin
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

mod managers;
use slint::ComponentHandle;
use regex::Regex;

slint::include_modules!();

// Main function
fn main() -> Result<(), slint::PlatformError> {

    // Create the UI window
    let ui = Scene::new()?;

    // When a user types in the nickname field, we want to validate it and update the UI. 
    // We use a weak reference to avoid borrowing issues
    let ui_handle = ui.as_weak();
    ui.on_validate_nickname(move |nickname| {
        let ui = ui_handle.unwrap();

        // Check if the nickname is valid
        let (is_valid, error_msg) = validate_nickname(&nickname);

        // Update the UI with results
        ui.set_nickname_valid(is_valid);
        ui.set_nickname_error(error_msg.into());
    });

    // Setup what happens when a user presses Enter
    let ui_handle = ui.as_weak();
    ui.on_nickname_entered(move |nickname| {
        ui_handle.unwrap();
        let (is_valid, _) = validate_nickname(&nickname);

        if is_valid {
            println!("Valid nickname: {}", nickname);

            // TODO: enter to the next screen

        } else if nickname == "cubelius" || nickname == "Cubelius" {

            // TODO: do this old meme
            std::process::exit(0);
        } else {
            println!("Invalid nickname: {}", nickname)
        }
    }); 
    
    // Start the application
    ui.run()
}

// Function that checks if the nickname is valid
fn validate_nickname(nickname: &str) -> (bool, &'static str) {

    // Too short
    if nickname.len() < 3 {
        return (false, "Minimum 3 characters");
    }

    // Too long
    if nickname.len() > 16 {
        return (false, "Maximum 16 characters");
    }

    // Contains only allowed characters
    let valid_chars = Regex::new(r"^[a-zA-Z0-9_]+$").unwrap();
    if !valid_chars.is_match(nickname) {
        return (false, "Only English letters, numbers and _ allowed");
    }
    
    (true, "")
}