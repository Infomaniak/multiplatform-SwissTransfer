// swift-tools-version:5.10
import PackageDescription

let package = Package(
    name: "SwissTransfer-Multiplatform",
    platforms: [
        .iOS(.v14),
        .macOS(.v11)
    ],
    products: [
        .library(name: "STCore", targets: ["Core"]),
        .library(name: "STDatabase", targets: ["Database"]),
        .library(name: "STNetwork", targets: ["Network"])
    ],
    targets: [
        .binaryTarget(
            name: "Core",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/0.0.3/STCore.xcframework.zip",
            checksum: "85e9fbb406fb2c8b20788c953c434108863186149c9d01594ef9deddc4a2a094"
        ),
        .binaryTarget(
            name: "Database",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/0.0.3/STDatabase.xcframework.zip",
            checksum: "9844ecc904a620caf66f53079f08f43f0c084be685be09b4ccc6a7e9b1b5e3c5"
        ),
        .binaryTarget(
            name: "Network",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/0.0.3/STNetwork.xcframework.zip",
            checksum: "509d193a8931e5b5b3a12522095efb50e8e1e769b6f3f8adc2b6ab37cd3c4ec5"
        ),
    ]
)
