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
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/3.0.0/STCore.xcframework.zip",
            checksum: "add74d93f69b9bb57189a0b91d695e2f49b619a9590b1bb13367d873d8809b10"
        ),
        .binaryTarget(
            name: "Database",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/3.0.0/STDatabase.xcframework.zip",
            checksum: "0ae33fd6ef34f5fc8f8ed84837a06c75819f9ead53b50bbe3b05fee86547b849"
        ),
        .binaryTarget(
            name: "Network",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/3.0.0/STNetwork.xcframework.zip",
            checksum: "08fa8348284af051aad57759950a517d72c742f67d33a5d26742df173ebf9a32"
        ),
    ]
)
