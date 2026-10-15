# Donut Pay Tracker

A client-side Fabric mod for Minecraft Java Edition 1.21.11. It watches Donut SMP server receipt messages and displays incoming payments that meet a configurable minimum.

## Features

- Press **O** to open the tracker.
- Set a minimum payment and select **Save minimum**. The setting is saved in `config/paytracker.properties`.
- See the total and the 100 most recent matching payments, including the payer's name and amount.
- Only server receipt messages are read. The mod does not send commands or change payments.

The payer list and total last for the current game session. Payments below the minimum are not added.

## Build

Install JDK 21 and Gradle, then run:

```powershell
gradle build
```

The built mod is written to `build/libs/paytracker-1.0.0.jar`. Install Fabric Loader for Minecraft 1.21.11 and Fabric API for 1.21.11, then put the jar in your Minecraft `mods` folder.

## Payment message format

The tracker recognizes server game messages matching:

```text
<player> paid you $ <amount>
```

The message format can vary by server update. If your payment receipt looks different, share the exact in-game text and the matcher can be adjusted.
