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
use slint::{ComponentHandle, SharedString};
use regex::Regex;

slint::include_modules!();

// Main function

fn main() -> Result<(), slint::PlatformError> {
    let ui = Scene::new()?;
    
    // Nickname input handling
    let ui_weak = ui.as_weak();
    ui.on_validate_nickname(move |new_text: SharedString| {
        let ui = ui_weak.unwrap();
        let mut nick = new_text.to_string();

        // Fucking Slint, why don't you have a way to set the max length of a text input?
        let max_chars = 16;
        if nick.chars().count() > max_chars {
            nick = nick.chars().take(max_chars).collect();
            ui.set_nickname_text(SharedString::from(&nick));
        }

        println!("Nickname changed: {}", nick);

        let (is_valid, error_msg) = validate_nickname(&nick);
        ui.set_nickname_valid(is_valid);
        ui.set_nickname_error(error_msg.into());

        // Return the validated nickname
        SharedString::from(nick)
    });

    // Callback for when the nickname is entered
    let ui_weak2 = ui.as_weak();
    ui.on_nickname_entered(move |nickname| {
        let ui = ui_weak2.unwrap();
        let (is_valid, _) = validate_nickname(&nickname);
        if is_valid {
            println!("Valid nickname: {}", nickname);
        } else if nickname.eq_ignore_ascii_case("cubelius") {
            std::process::exit(0);
        } else {
            println!("Invalid nickname: {}", nickname);
        }
    });

    ui.run()
}

// Function that checks if the nickname is valid
fn validate_nickname(nickname: &str) -> (bool, &'static str) {
    // Too short
    if nickname.len() < 3 {
        return (false, "Minimum 3 characters");
    }

    // Contains only allowed characters
    let valid_chars = Regex::new(r"^[a-zA-Z0-9_]+$").unwrap();
    if !valid_chars.is_match(nickname) {
        return (false, "Only English letters, numbers and _ allowed");
    }
    
    (true, "")
}