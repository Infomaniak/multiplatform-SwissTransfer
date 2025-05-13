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
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/4.1.0/STCore.xcframework.zip",
            checksum: "4563317fd00c4b4d834bd7aee5d56ccfa14d300b1fe0d725499a516f39aaf514"
        ),
        .binaryTarget(
            name: "Database",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/4.1.0/STDatabase.xcframework.zip",
            checksum: "50078c5c3c0f5a1e3605bb6bc35616baa7155403570748d879f6d94a51d8eb8f"
        ),
        .binaryTarget(
            name: "Network",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/4.1.0/STNetwork.xcframework.zip",
            checksum: "9ea0df2d2f88e5e90f119575c71de950779e70ad011aaef76a2650a2692535eb"
        ),
    ]
)
