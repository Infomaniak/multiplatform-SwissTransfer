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
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/5.3.2/STCore.xcframework.zip",
            checksum: "13c18cb1a317d1b6ac2f0e451d6eed0d5cc9ac234c22c33f75778252daaa71e8"
        ),
        .binaryTarget(
            name: "Database",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/5.3.2/STDatabase.xcframework.zip",
            checksum: "5ab8d24dff8c4d2582a274432ca28fb52afafae0c4b2a0a57b22db3d29750a46"
        ),
        .binaryTarget(
            name: "Network",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/5.3.2/STNetwork.xcframework.zip",
            checksum: "56a6c7856d8d51f19c5e53e69a80ea22ef5cd879ab8be31020e071a9fdbaa0b0"
        ),
    ]
)
