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
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/1.0.4/STCore.xcframework.zip",
            checksum: "29200389af2222c6f35a6c14a0956a264332241de5686ed00340ec62b5c33bca"
        ),
        .binaryTarget(
            name: "Database",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/1.0.4/STDatabase.xcframework.zip",
            checksum: "96a7de769983058a026eab94ad48da27a05b69bc34239de1cf7cb58fb34a72e2"
        ),
        .binaryTarget(
            name: "Network",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/1.0.4/STNetwork.xcframework.zip",
            checksum: "ebe0ea97e2fce4b6ed6f7befe5411f60611494e552fd942bb4faa0b519cac1f6"
        ),
    ]
)
